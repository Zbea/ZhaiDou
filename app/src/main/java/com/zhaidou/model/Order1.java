package com.zhaidou.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wangclark on 15/7/24.
 */
public class Order1 implements Serializable{

    public int orderId;
    public String orderCode;
    public int status;
    public String orderShowStatus;
    public int buyerId;
    public String itemTotalAmount;
    public String orderPayAmount;
    public String orderActualAmount;
    public String orderTotalAmount;
    public String discountAmount;
    public String deliveryFee;
    public String creationTime;
    public String updatedTime;
    public String storeId;
    public String remark;
    public long orderRemainingTime;
    public long orderRemainingTimeType;

    public List<Store> childOrderPOList;
    public Address deliveryAddressPO;

    @Override
    public String toString() {
        return "Order1{" +
                "orderId=" + orderId +
                ", orderCode='" + orderCode + '\'' +
                ", status=" + status +
                ", orderShowStatus='" + orderShowStatus + '\'' +
                ", buyerId=" + buyerId +
                ", itemTotalAmount='" + itemTotalAmount + '\'' +
                ", orderPayAmount='" + orderPayAmount + '\'' +
                ", orderActualAmount='" + orderActualAmount + '\'' +
                ", orderTotalAmount='" + orderTotalAmount + '\'' +
                ", discountAmount='" + discountAmount + '\'' +
                ", deliveryFee='" + deliveryFee + '\'' +
                ", creationTime='" + creationTime + '\'' +
                ", updatedTime='" + updatedTime + '\'' +
                ", storeId='" + storeId + '\'' +
                ", remark='" + remark + '\'' +
                ", childOrderPOList=" + childOrderPOList +
                ", deliveryAddressPO=" + deliveryAddressPO +
                '}';
    }
}
