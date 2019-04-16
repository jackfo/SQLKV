package com.cfs.sqlkv.store.access.raw.data;


import com.cfs.sqlkv.service.io.FormatableBitSet;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author zhengxiaokang
 * @Description An allocation extent row manages the page status of page in the extent.
 * AllocExtent is externalizable and is written to the AllocPage directly,
 * without being converted to a row first.
 * <p>
 * <PRE>
 * formatId 格式ID
 * purpose  管理页面的状态
 * upgrade
 * diskLayout
 * extentOffset(long) 扩展页首页开始的游标
 * extentStart(long)  首个逻辑页
 * extentEnd(long) the last page this extent can ever hope to manage
 * extentPageNums(int) 当前区页的数目
 * extentStatus(int) status bits for the whole extent.
 * HAS_DEALLOCATED - most likely, this extent has a deallocated
 * page somewhere
 * If !HAD_DEALLOCATED, the extent has no deallocated page
 * HAS_FREE - most likely, this extent has a free page somewhere
 * If !HAS_FREE, there is no free page in the extent
 * ALL_FREE - most likely, this extent only has free pages, good
 * candidate for shrinking the file.
 * If !ALL_FREE, the extent is not all free
 * HAS_UNFILLED_PAGES - most likely, this extent has unfilled pages.
 * if !HAS_UNFILLED_PAGES, all pages are filled
 * KEEP_UNFILLED_PAGES - this extent keeps track of unfilled pages
 * (post v1.3).  If not set, this extent has no notion of
 * unfilled page and has no unFilledPage bitmap.
 * NO_DEALLOC_PAGE_MAP - this extents do not have a dealloc and a
 * free page bit maps.  Prior to 2.0, there are 2 bit
 * maps, a deallocate page bit map and a free page bit
 * map.  Cloudscape 2.0 and later merged the dealloc page
 * bit map into the free page bit map.
 * RETIRED - this extent contains only 'retired' pages, never use
 * any page from this extent.  The pages don't actually
 * exist, i.e., it maps to nothing (physicalOffset is
 * garbage).  The purpose of this extent is to blot out a
 * range of logical page numbers that no longer exists
 * for this container.  Use this to reuse a physical page
 * when a logical page has exhausted all recordId or for
 * logical pages that has been shrunk out.
 * preAllocLength(int)  the number of pages that have been preallocated
 * reserved1(int)
 * reserved2(long)	reserved for future use
 * reserved3(long)	reserved for future use
 * FreePages(bit)	bitmap of free pages
 * Bit[i] is ON iff page i is free for immediate (re)use.
 * [
 * on disk version before 2.0
 * deAllocPages(bit) bitmap of deallocated pages
 * Bit[i] is ON iff page i has been deallocated.
 * ]
 * unFilledPages(bit)	bitmap of pages that has free space
 * Bit[i] is ON if page i is likely to be < 1/2 full
 * <p>
 * com.cfs.sqlkv.service.io.FormatableBitSet is used to store the bit map.
 * FormatableBitSet is an externalizable class.
 *
 *
 *
 * <PRE>
 * A page can have the following logical state:
 * <BR>Free - a page that is free to be used
 * <BR>Valid - a page that is currently in use
 * <p>
 * There is another type of transitional pages which pages that have been
 * allocated on disk but has not yet been used.  These pages are Free.
 * <p>
 * Bit[K] freePages
 * Bit[i] is ON iff page i maybe free for reuse.  User must get the
 * dealloc page lock on the free page to make sure the transaction.
 * <p>
 * K is the size of the bit array, it must be >= length.
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-19 16:43
 * @see AllocPage
 */
public class AllocExtent {

    private long extentOffset;
    private long extentStart;
    private long extentEnd;
    private int extentPageNums;
    int extentStatus;
    private int preAllocLength;


    /**
     * 区的相关状态
     */
    private static final int HAS_DEALLOCATED = 0x1;
    /**
     * 区存在空闲页
     */
    private static final int HAS_FREE = 0x2;
    /**
     * 区里面只存在空闲页
     */
    private static final int ALL_FREE = 0x4;
    /**
     * 区存在未装满的空闲页
     */
    private static final int HAS_UNFILLED_PAGES = 0x10;
    private static final int KEEP_UNFILLED_PAGES = 0x10000000;
    private static final int NO_DEALLOC_PAGE_MAP = 0x20000000;
    private static final int RETIRED = 0x8;
    protected static final int ALLOCATED_PAGE = 0;
    protected static final int DEALLOCATED_PAGE = 1;
    protected static final int FREE_PAGE = 2;

    FormatableBitSet freePages;
    FormatableBitSet unFilledPages;

    public AllocExtent() {
    }


    /**
     * @param offset   物理偏移量
     * @param start    开始的页号
     * @param pageNums 设置当前区存在多少页
     * @param pagesize 页面大小
     */
    protected AllocExtent(long offset, long start, int pageNums, int pagesize, int maxlength) {
        this.extentOffset = offset;
        this.extentStart = start;
        this.extentEnd = start + maxlength - 1;
        this.extentPageNums = pageNums;
        preAllocLength = extentPageNums;

        /**
         * 如果页的数量大于0 则设置当前页的状态是有空闲页并且开始所有页都是空闲页
         * */
        if (pageNums > 0) {
            extentStatus = HAS_FREE | ALL_FREE;
        } else {
            extentStatus = 0;
        }

        extentStatus |= KEEP_UNFILLED_PAGES;
        extentStatus |= NO_DEALLOC_PAGE_MAP;

        /**
         * 设计将所有的页号通过位图来进行管理
         * 如果页号所计算出来的位图大于初始化位图最大长度 则赋值为最大长度
         * */
        int numbits = (1 + (pageNums / 8)) * 8;

        if (numbits > maxlength) {
            numbits = maxlength;
        }

        /**
         * 构建空闲页和未装满页的位图
         * */
        freePages = new FormatableBitSet(numbits);
        unFilledPages = new FormatableBitSet(numbits);


        /**
         * 将位图对应的位置设置进去,通过1来进行表示
         * */
        for (int i = 0; i < pageNums; i++) {
            freePages.set(i);
        }
    }


    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(extentOffset);
        out.writeLong(extentStart);
        out.writeLong(extentEnd);
        out.writeInt(extentPageNums);
        out.writeInt(extentStatus);
        out.writeInt(preAllocLength);
        freePages.writeExternal(out);
        unFilledPages.writeExternal(out);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        extentOffset = in.readLong();
        extentStart = in.readLong();
        extentEnd = in.readLong();
        extentPageNums = in.readInt();
        extentStatus = in.readInt();
        preAllocLength = in.readInt();
        freePages = new FormatableBitSet();
        freePages.readExternal(in);
        if ((extentStatus & KEEP_UNFILLED_PAGES) == KEEP_UNFILLED_PAGES) {
            unFilledPages = new FormatableBitSet();
            unFilledPages.readExternal(in);
        } else {
            // make sure there are enough space
            unFilledPages = new FormatableBitSet(freePages.getLength());
            extentStatus |= KEEP_UNFILLED_PAGES;
        }

    }


    /**
     * @description 根据分配页获取下一个页
     * 首先检测当前区是否存在空闲页
     */
    protected long getFreePageNumber(long pageNumber) {
        if (mayHaveFreePage()) {
            int i;
            //最后一个分配页可能是先前一个分配,如果是分配页是前一个区的页,则从开始进行获取
            if (pageNumber < extentStart) {
                //从位图开始位置获取第一个未使用的空闲页
                i = freePages.anySetBit();
            } else {
                //从位图的pageNumber-extentStart开始 主要是为了获取其当前位图开始的位置 因为从当前开始页号开始位置记做0
                i = freePages.anySetBit((int) (pageNumber - extentStart));
            }
            //如果i不为1,表示获取的是从区开始的第i页,最终返回真实的页号
            if (i != -1) {
                return i + extentStart;
            }
            /**
             * 如果页号小于区开始页号,表明在当前去接分配不到页,所以设置当前区为空闲区
             * */
            if (pageNumber < extentStart) {
                setExtentFreePageStatus(false);
            }

        }

        /**
         *返回的是下一个区开始位置
         * */
        return extentStart + extentPageNums;
    }

    /**
     * 如果是false则设置区间没有空闲页
     * 是true设置区间存在空闲页
     */
    private void setExtentFreePageStatus(boolean hasFree) {
        if (hasFree) {
            extentStatus |= HAS_FREE;
        } else {
            extentStatus &= ~HAS_FREE;
        }
    }

    /**
     * 检测当前区是否还有空闲页
     *
     * @return 如果有返回true
     */
    private boolean mayHaveFreePage() {
        return (extentStatus & HAS_FREE) != 0;
    }

    /**
     * 获取当前区的最后页号
     */
    protected long getLastPagenum() {
        return extentStart + extentPageNums - 1;
    }

    /**
     *
     */
    protected void allocPage(long pageNumber) {
        //获取其在位图的位置
        int bitnum = (int) (pageNumber - extentStart);

        if (bitnum >= freePages.getLength()) {
            int numbits = (1 + (bitnum / 8)) * 8;
            if (numbits > (int) (extentEnd - extentStart + 1)) {
                numbits = (int) (extentEnd - extentStart + 1);
            }
            freePages.grow(numbits);
            unFilledPages.grow(numbits);
        }
        int numPageAlloced = (int) (pageNumber - extentStart + 1);

        if (numPageAlloced > extentPageNums) {
            extentPageNums = numPageAlloced;
        }
        //这样设置表示当前页不在空闲
        freePages.clear(bitnum);
    }

    protected static int MAX_RANGE(int availspace) {
        int bookkeeping = 8 /* offset */ +
                8 /* start */ +
                8 /* end */ +
                4 /* length */ +
                4 /* status */ +
                4 /* preAllocLength */;
        availspace -= bookkeeping;
        availspace /= 3;
        if (availspace <= 0) {
            return 0;
        }
        return FormatableBitSet.maxBitsForSpace(availspace);
    }

    /**
     * @param pageNumber 页号
     * @desperation 根据页号获取对应的面的状态
     */
    protected int getPageStatus(long pageNumber) {
        int status;
        int bitnum = (int) (pageNumber - extentStart);
        if (freePages.isSet(bitnum)) {
            status = FREE_PAGE;
        } else {
            status = ALLOCATED_PAGE;
        }
        return status;
    }

    /**
     * 获取当前区第一页
     */
    protected long getFirstPagenum() {
        return extentStart;
    }

    protected long getUnfilledPageNumber(long pagenum) {
        if ((extentStatus & HAS_UNFILLED_PAGES) == 0) {
            return BaseContainerHandle.INVALID_PAGE_NUMBER;
        }
        int i = unFilledPages.anySetBit();
        if (i != -1) {
            if (i + extentStart != pagenum) {
                return i + extentStart;
            } else {
                i = unFilledPages.anySetBit(i);
                if (i != -1) {
                    return i + extentStart;
                }
            }
        }
        return BaseContainerHandle.INVALID_PAGE_NUMBER;
    }


    public long getNextValidPageNumber(long prevPageNumber) {
        long pageNum;
        long lastpage = getLastPagenum();
        if (prevPageNumber < extentStart)
            pageNum = extentStart;
        else
            pageNum = prevPageNumber + 1;

        while (pageNum <= lastpage) {
            int status = getPageStatus(pageNum);
            if (status == ALLOCATED_PAGE)
                break;
            pageNum++;
        }

        if (pageNum > lastpage)
            pageNum = BaseContainerHandle.INVALID_PAGE_NUMBER;
        return pageNum;
    }
}
