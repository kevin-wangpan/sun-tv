package com.jiaoyang.tv.data;

public class PaginatedData<T> {

    public int itemsPerPage;
    public int totalItems;
    public int pageIndex;
    public int totalPages;
    public T[] items;

}
