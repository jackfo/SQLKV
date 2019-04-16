package com.cfs.sqlkv.store.access.raw.data;


/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-17 17:16
 */
public class AllocationCache {

    private int numExtents;
    /**
     * 所有的有效页的pageNumber在lowRange和hiRange之间
     */
    private long[] lowRange;
    private long[] hiRange;
    private boolean[] isDirty;
    private AllocExtent[] extents;
    private long[] extentPageNums;
    private boolean isValid;

    protected AllocationCache() {
        numExtents = 0;
        isValid = false;
    }

    protected long getAllocPageNumber(BaseContainerHandle handle, long pageNumber, long firstAllocPageNumber) {
        for (int i = 0; i < numExtents; i++) {
            if (lowRange[i] <= pageNumber && pageNumber <= hiRange[i]) {
                return extentPageNums[i];
            }
        }
        float a = 3.0f;
        if (!isValid) {
            validate(handle, firstAllocPageNumber);
            for (int i = 0; i < numExtents; i++) {
                if (lowRange[i] <= pageNumber && pageNumber <= hiRange[i])
                    return extentPageNums[i];
            }
        }
        return BaseContainerHandle.INVALID_PAGE_NUMBER;
    }

    protected long getLastPageNumber(BaseContainerHandle handle, long firstAllocPageNumber) {
        if (!isValid) {
            validate(handle, firstAllocPageNumber);
        }
        return hiRange[numExtents - 1];
    }


    protected void invalidate(AllocPage allocPage, long allocPagenum) {
        isValid = false;
        if (numExtents == 0) {
            return;
        }
        for (int i = 0; i < numExtents; i++) {
            if (extentPageNums[i] == allocPagenum) {
                if (allocPage != null && extents[i] != null && isDirty[i]) {
                    isDirty[i] = false;
                }
                extents[i] = null;
                return;
            }
        }
        if (allocPagenum > hiRange[numExtents - 1]) {
            return;
        }
    }

    /**
     * 验证缓存,保证缓存是合法的
     * <p>
     * 1.根据页号获取AllocPage
     * 2.设置当前缓存的范围
     */
    private void validate(BaseContainerHandle handle, long firstAllocPageNumber) {
        if (numExtents == 0) {
            long pagenum = firstAllocPageNumber;
            while (!isValid) {
                growArrays(++numExtents);
                Object obj = handle.getAllocPage(pagenum);
                AllocPage allocPage;
                try {
                    allocPage = (AllocPage) obj;
                } catch (ClassCastException e) {
                    throw new RuntimeException(e.getMessage() + "  [pagenum is " + pagenum + "]");
                }
                setArrays(numExtents - 1, allocPage);
                if (allocPage.isLast()) {
                    isValid = true;
                } else {
                    pagenum = allocPage.getNextAllocPageNumber();
                }
                allocPage.unlatch();
            }
        } else {
            //遍历所有的去 找到一个空区,获取区对应的页,将分配页设置进去,这一步是保证所有分区都是都存在具备分配页
            for (int i = 0; i < numExtents - 1; i++) {
                if (extents[i] == null) {
                    AllocPage allocPage = (AllocPage) handle.getAllocPage(extentPageNums[i]);
                    setArrays(i, allocPage);
                    allocPage.unlatch();
                }
            }

            long pagenum = extentPageNums[numExtents - 1];
            while (!isValid) {
                AllocPage allocPage = (AllocPage) handle.getAllocPage(pagenum);
                if (extents[numExtents - 1] == null) {
                    setArrays(numExtents - 1, allocPage);
                }
                if (!allocPage.isLast()) {
                    growArrays(++numExtents);
                    pagenum = allocPage.getNextAllocPageNumber();
                } else {
                    isValid = true;
                }

                allocPage.unlatch();
            }
        }
    }

    private void growArrays(int size) {
        int oldLength;
        if (lowRange == null || lowRange.length == 0) {
            oldLength = 0;
        } else {
            oldLength = lowRange.length;
        }
        if (oldLength >= size) {
            return;
        }
        long[] saveLow = lowRange;
        long[] saveHi = hiRange;
        AllocExtent[] saveExtents = extents;
        boolean[] saveDirty = isDirty;
        long[] savePageNums = extentPageNums;
        lowRange = new long[size];
        hiRange = new long[size];
        isDirty = new boolean[size];
        extents = new AllocExtent[size];
        extentPageNums = new long[size];
        if (oldLength > 0) {
            System.arraycopy(saveLow, 0, lowRange, 0, saveLow.length);
            System.arraycopy(saveHi, 0, hiRange, 0, saveHi.length);
            System.arraycopy(saveDirty, 0, isDirty, 0, saveDirty.length);
            System.arraycopy(saveExtents, 0, extents, 0, saveExtents.length);
            System.arraycopy(savePageNums, 0, extentPageNums, 0, savePageNums.length);
        }
        for (int i = oldLength; i < size; i++) {
            lowRange[i] = BaseContainerHandle.INVALID_PAGE_NUMBER;
            hiRange[i] = BaseContainerHandle.INVALID_PAGE_NUMBER;
            isDirty[i] = false;
            extentPageNums[i] = BaseContainerHandle.INVALID_PAGE_NUMBER;
            extents[i] = null;
        }
    }

    /**
     * 将分配页设置到去的当前位置
     */
    private void setArrays(int i, AllocPage allocPage) {
        AllocExtent extent = allocPage.getAllocExtent();
        extents[i] = extent;
        lowRange[i] = extent.getFirstPagenum();
        hiRange[i] = extent.getLastPagenum();
        extentPageNums[i] = allocPage.getPageNumber();
    }

    protected void reset() {
        numExtents = 0;
        isValid = false;
        if (lowRange != null) {
            for (int i = 0; i < lowRange.length; i++) {
                lowRange[i] = BaseContainerHandle.INVALID_PAGE_NUMBER;
                hiRange[i] = BaseContainerHandle.INVALID_PAGE_NUMBER;
                extentPageNums[i] = BaseContainerHandle.INVALID_PAGE_NUMBER;
                extents[i] = null;
                isDirty[i] = false;
            }
        }
    }

    /**
     * 获取页面状态
     * 遍历所有的分区,找到页面所对应的分区
     */
    protected int getPageStatus(BaseContainerHandle handle, long pageNumber, long firstAllocPageNumber) {
        AllocExtent extent = null;
        for (int i = 0; i < numExtents; i++) {
            if (lowRange[i] <= pageNumber && pageNumber <= hiRange[i]) {
                extent = extents[i];
                break;
            }
        }
        if (extent == null) {
            if (!isValid) {
                validate(handle, firstAllocPageNumber);
            }
            for (int i = 0; i < numExtents; i++) {
                if (lowRange[i] <= pageNumber && pageNumber <= hiRange[i]) {
                    extent = extents[i];
                    break;
                }
            }
        }
        //获取对应分区的页面状态
        return extent.getPageStatus(pageNumber);
    }

    protected long getUnfilledPageNumber(BaseContainerHandle handle, long firstAllocPageNumber, long pagenum) {
        if (!isValid) {
            validate(handle, firstAllocPageNumber);
        }
        if (pagenum == BaseContainerHandle.INVALID_PAGE_NUMBER) {
            for (int i = 0; i < numExtents; i++) {
                if (extents[i] != null)
                    return extents[i].getUnfilledPageNumber(pagenum);
            }
        } else {
            for (int i = 0; i < numExtents; i++) {
                if (pagenum <= hiRange[i]) {
                    if (extents[i] != null) {
                        return extents[i].getUnfilledPageNumber(pagenum);
                    }
                }
            }
        }
        return BaseContainerHandle.INVALID_PAGE_NUMBER;
    }


    /**
     * 设置所有分区处于未验证的状态,并将其设置为空
     */
    protected void invalidate() {
        for (int i = 0; i < numExtents; i++) {
            isDirty[i] = false;
            extents[i] = null;
        }
        isValid = false;
    }


    public long getNextValidPage(BaseContainerHandle handle, long pageNumber, long firstAllocPageNumber) {
        int extentNumber;
        if (!isValid)
            validate(handle, firstAllocPageNumber);

        if (numExtents == 0)
            return BaseContainerHandle.INVALID_PAGE_NUMBER;
        AllocExtent extent = null;
        for (extentNumber = 0; extentNumber < numExtents; extentNumber++) {
            if (pageNumber < hiRange[extentNumber]) {
                extent = extents[extentNumber];
                break;
            }
        }
        if (extent == null) {
            return BaseContainerHandle.INVALID_PAGE_NUMBER;
        }
        long nextValidPage = BaseContainerHandle.INVALID_PAGE_NUMBER;

        while (extentNumber < numExtents) {
            extent = extents[extentNumber];
            nextValidPage = extent.getNextValidPageNumber(pageNumber);
            if (nextValidPage != BaseContainerHandle.INVALID_PAGE_NUMBER)
                break;

            extentNumber++;
        }
        return nextValidPage;
    }


}
