package com.zhaidou.model;/**
 * Created by wangclark on 15/12/3.
 */

import java.util.List;

/**
 * User: Scoield(553899626@qq.com)
 * Date: 2015-12-03
 * Time: 17:00
 * Description:订单详情
 * FIXME
 */
public class OrderDetail {
    public String storeId;
    public String storeName;
    public String supplierCode;
    public String supplierName;
    public String shippingTime;
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
    public String buyerNick;
    public String buyerMobile;
    public String payType;
    public String discountAmount;
    public String itemTotalAmount;
    public int orderTbAmount;
    public String orderTotalAmount;
    public String orderPayAmount;
    public String orderActualAmount;
    public String deliveryFee;
    public String creationTime;
    public String updatedTime;
    public String payTime;
    public int isEnterprise;
    public int pointConsumptionType;
    public int orderType;
    public String remark;


    public Address deliveryAddressPO;

    public List<OrderItem1> orderItemPOList;

}
