package com.seewo.palette.callback;

/**
 * 页面相关操作回调接口
 */
public interface PageChangeCall {

    void PageAddCall(int pagenum, int pageindex);

    void PagePreCall(int pageindex);

    void PageNextCall(int pageindex);
}
