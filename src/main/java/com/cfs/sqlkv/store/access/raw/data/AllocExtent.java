package com.cfs.sqlkv.store.access.raw.data;

import com.cfs.sqlkv.io.FormatableBitSet;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author zhengxiaokang
 * @Description An allocation extent row manages the page status of page in the extent.
 * 	AllocExtent is externalizable and is written to the AllocPage directly,
 * 	without being converted to a row first.
 * 	<P>
 * 	<PRE>
 *      formatId 格式ID
 * 	    purpose  管理页面的状态
 * 	    upgrade
 *      diskLayout
 * 		extentOffset(long) 扩展页首页开始的游标
 * 		extentStart(long)  首个逻辑页
 * 		extentEnd(long) the last page this extent can ever hope to manage
 * 		extentLength(int) the number of pages allocated in this extent
 * 		extentStatus(int) status bits for the whole extent.
 * 				HAS_DEALLOCATED - most likely, this extent has a deallocated
 *                         page somewhere
 * 						If !HAD_DEALLOCATED, the extent has no deallocated page
 * 				HAS_FREE - most likely, this extent has a free page somewhere
 * 						If !HAS_FREE, there is no free page in the extent
 * 				ALL_FREE - most likely, this extent only has free pages, good
 *                         candidate for shrinking the file.
 * 						If !ALL_FREE, the extent is not all free
 * 				HAS_UNFILLED_PAGES - most likely, this extent has unfilled pages.
 * 						if !HAS_UNFILLED_PAGES, all pages are filled
 * 				KEEP_UNFILLED_PAGES - this extent keeps track of unfilled pages
 * 						(post v1.3).  If not set, this extent has no notion of
 * 						unfilled page and has no unFilledPage bitmap.
 * 				NO_DEALLOC_PAGE_MAP - this extents do not have a dealloc and a
 * 						free page bit maps.  Prior to 2.0, there are 2 bit
 * 						maps, a deallocate page bit map and a free page bit
 * 						map.  Cloudscape 2.0 and later merged the dealloc page
 * 						bit map into the free page bit map.
 * 				RETIRED - this extent contains only 'retired' pages, never use
 *                         any page from this extent.  The pages don't actually
 *                         exist, i.e., it maps to nothing (physicalOffset is
 *                         garbage).  The purpose of this extent is to blot out a
 *                         range of logical page numbers that no longer exists
 *                         for this container.  Use this to reuse a physical page
 *                         when a logical page has exhausted all recordId or for
 *                         logical pages that has been shrunk out.
 * 		preAllocLength(int)  the number of pages that have been preallocated
 * 		reserved1(int)
 * 		reserved2(long)	reserved for future use
 * 		reserved3(long)	reserved for future use
 * 		FreePages(bit)	bitmap of free pages
 * 				Bit[i] is ON iff page i is free for immediate (re)use.
 * 		[
 * 		    on disk version before 2.0
 * 				deAllocPages(bit) bitmap of deallocated pages
 * 				Bit[i] is ON iff page i has been deallocated.
 * 		]
 * 		unFilledPages(bit)	bitmap of pages that has free space
 * 				Bit[i] is ON if page i is likely to be < 1/2 full
 *
 * 		org.apache.derby.iapi.services.io.FormatableBitSet is used to store the bit map.
 *             FormatableBitSet is an externalizable class.
 *
 *
 *
 * 	<PRE>
 * 	A page can have the following logical state:
 * 	<BR>Free - a page that is free to be used
 * 	<BR>Valid - a page that is currently in use
 * 	<P>
 * 	There is another type of transitional pages which pages that have been
 * 	allocated on disk but has not yet been used.  These pages are Free.
 * 	<P>
 * 	Bit[K] freePages
 * 		Bit[i] is ON iff page i maybe free for reuse.  User must get the
 * 		dealloc page lock on the free page to make sure the transaction.
 * 	<P>
 * 	K is the size of the bit array, it must be >= length.
 *
 * 	@see AllocPage
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-19 16:43
 */
public class AllocExtent {

    private long extentOffset;
    private long extentStart;
    private long extentEnd;
    private int extentLength;
    int extentStatus;
    private int preAllocLength;
    private int reserved1;
    private long reserved2;
    private long reserved3;

    private static final int HAS_DEALLOCATED = 0x1;
    private static final int HAS_FREE = 0x2;
    private static final int ALL_FREE = 0x4;
    private static final int HAS_UNFILLED_PAGES = 0x10;
    private static final int KEEP_UNFILLED_PAGES = 0x10000000;
    private static final int NO_DEALLOC_PAGE_MAP = 0x20000000;
    private static final int RETIRED = 0x8;

    protected static final int ALLOCATED_PAGE = 0;
    protected static final int DEALLOCATED_PAGE = 1;
    protected static final int FREE_PAGE = 2;

    FormatableBitSet freePages;
    FormatableBitSet unFilledPages;

    public AllocExtent() { }


    protected AllocExtent(long offset, // physical offset
                          long start,  // starting logical page number
                          int length,  // how many pages are in this extent
                          int pagesize, // size of all the pages in the extent
                          int maxlength) // initial size of the bit map arrays
    {
        this.extentOffset = offset;
        this.extentStart = start;
        this.extentEnd = start+maxlength-1;

        this.extentLength = length;
        preAllocLength = extentLength;

        if (length > 0){
            extentStatus = HAS_FREE | ALL_FREE ;
        } else{
            extentStatus = 0;
        }

        extentStatus |= KEEP_UNFILLED_PAGES;
        extentStatus |= NO_DEALLOC_PAGE_MAP;

        int numbits = (1+(length/8))*8;
        if (numbits > maxlength){
            numbits = maxlength;
        }

        freePages = new FormatableBitSet(numbits);
        unFilledPages = new FormatableBitSet(numbits);


        for (int i = 0; i < length; i++){
            freePages.set(i);
        }
    }


    public void writeExternal(ObjectOutput out) throws IOException{
        out.writeLong(extentOffset);
        out.writeLong(extentStart);
        out.writeLong(extentEnd);
        out.writeInt(extentLength);
        out.writeInt(extentStatus);
        out.writeInt(preAllocLength);
        /**
         * 三个保留字段
         * */
        out.writeInt(0);
        out.writeLong(0);
        out.writeLong(0);
        freePages.writeExternal(out);
        unFilledPages.writeExternal(out);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        extentOffset = in.readLong();
        extentStart	= in.readLong();
        extentEnd	= in.readLong();
        extentLength = in.readInt();
        extentStatus = in.readInt();
        preAllocLength = in.readInt();
        reserved1 = in.readInt();
        reserved2 = in.readLong();
        reserved3 = in.readLong();
        freePages = new FormatableBitSet();
        freePages.readExternal(in);

        // this extent is created before 2.0
        if ((extentStatus & NO_DEALLOC_PAGE_MAP) == 0) {
            FormatableBitSet deAllocPages = new FormatableBitSet();
            deAllocPages.readExternal(in);
            // fold this into free page bit map
            freePages.or(deAllocPages);
            extentStatus |= NO_DEALLOC_PAGE_MAP;
        }

        if ((extentStatus & KEEP_UNFILLED_PAGES) == KEEP_UNFILLED_PAGES) {
            unFilledPages = new FormatableBitSet();
            unFilledPages.readExternal(in);
        } else {
            // make sure there are enough space
            unFilledPages = new FormatableBitSet(freePages.getLength());
            extentStatus |= KEEP_UNFILLED_PAGES;
        }

    }


}
