package com.zhaidou.model;/**
 * Created by wangclark on 15/12/3.
 */

import java.util.List;

/**
 * User: Scoield(553899626@qq.com)
 * Date: 2015-12-03
 * Time: 11:12
 * Description:商店实体类
 * FIXME
 */
public class Store {
    public String storeId;
    public String storeName;
    public String supplierCode;
    public String supplierName;
    public int quantity;
    public int isApplyCancel;
    public int orderId;
    public String orderCode;
    public String parentOrderCode;
    public String businessType;
    public int status;
    public String orderShowStatus;
    public int isDelete;
    public int buyerId;
    public String buyerEmail;
    public String buyerNick;
    public String buyerMobile;
    public String discountAmount;
    public String itemTotalAmount;
    public String orderTotalAmount;
    public String orderPayAmount;
    public String orderActualAmount;
    public String deliveryFee;
    public String creationTime;
    public String updatedTime;
    public int orderType;
    public boolean isExpand;

    public List<OrderItem1> orderItemPOList;
}
