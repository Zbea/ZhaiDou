package com.zhaidou.model;/**
 * Created by wangclark on 15/12/3.
 */

import java.io.Serializable;
import java.util.List;

/**
 * User: Scoield(553899626@qq.com)
 * Date: 2015-12-03
 * Time: 11:12
 * Description:商店实体类
 * FIXME
 */
public class Store implements Serializable{
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
    public int returnGoodsFlag;
    public int orderRemainingTime;

    //退换货
    public String createTime;
    public String statusShowName;
    public String returnFlowCode;
    public String actualAmount;
    public List<OrderItem1> mallReturnFlowDetailDTOList;
    //退换货----------------------end

    public Delivery deliveryPO;
    public DeliveryAddress deliveryAddressPO;

    public List<OrderItem1> orderItemPOList;

    @Override
    public String toString() {
        return "Store{" +
                "storeId='" + storeId + '\'' +
                ", storeName='" + storeName + '\'' +
                ", supplierCode='" + supplierCode + '\'' +
                ", supplierName='" + supplierName + '\'' +
                ", quantity=" + quantity +
                ", isApplyCancel=" + isApplyCancel +
                ", orderId=" + orderId +
                ", orderCode='" + orderCode + '\'' +
                ", parentOrderCode='" + parentOrderCode + '\'' +
                ", businessType='" + businessType + '\'' +
                ", status=" + status +
                ", orderShowStatus='" + orderShowStatus + '\'' +
                ", isDelete=" + isDelete +
                ", buyerId=" + buyerId +
                ", buyerEmail='" + buyerEmail + '\'' +
                ", buyerNick='" + buyerNick + '\'' +
                ", buyerMobile='" + buyerMobile + '\'' +
                ", discountAmount='" + discountAmount + '\'' +
                ", itemTotalAmount='" + itemTotalAmount + '\'' +
                ", orderTotalAmount='" + orderTotalAmount + '\'' +
                ", orderPayAmount='" + orderPayAmount + '\'' +
                ", orderActualAmount='" + orderActualAmount + '\'' +
                ", deliveryFee='" + deliveryFee + '\'' +
                ", creationTime='" + creationTime + '\'' +
                ", updatedTime='" + updatedTime + '\'' +
                ", orderType=" + orderType +
                ", isExpand=" + isExpand +
                ", returnFlowCode='" + returnFlowCode + '\'' +
                ", mallReturnFlowDetailDTOList=" + mallReturnFlowDetailDTOList +
                ", deliveryPO=" + deliveryPO +
                ", deliveryAddressPO=" + deliveryAddressPO +
                ", orderItemPOList=" + orderItemPOList +
                '}';
    }
}
