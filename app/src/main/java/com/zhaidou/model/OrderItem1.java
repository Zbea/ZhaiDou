package com.zhaidou.model;/**
 * Created by wangclark on 15/12/3.
 */

import java.io.Serializable;

/**
 * User: Scoield(553899626@qq.com)
 * Date: 2015-12-03
 * Time: 11:16
 * Description:订单项实体类
 * FIXME
 */
public class OrderItem1 implements Serializable{
    public String specifications;
    public int isCanApplyGuijiupei;
    public int guijiupeiId;
    public String businessType;
    public int orderItemId;
    public int productId;
    public String productCode;
    public String productName;
    public int productType;
    public int productSKUId;
    public String productSKUCode;
    public String price;
    public String marketPrice;
    public int quantity;
    public String pictureBigUrl;
    public String pictureSmallUrl;
    public String pictureMiddleUrl;
    public String favorableAmount1;
    public String paidAmount;

    //退换货
    public int returnFlowDetailId;
    public String remark;
    public String thumbnailPicUrl;
    public String salePrice;

    @Override
    public String toString() {
        return "OrderItem1{" +
                "specifications='" + specifications + '\'' +
                ", isCanApplyGuijiupei=" + isCanApplyGuijiupei +
                ", guijiupeiId=" + guijiupeiId +
                ", businessType='" + businessType + '\'' +
                ", orderItemId=" + orderItemId +
                ", productId=" + productId +
                ", productCode='" + productCode + '\'' +
                ", productName='" + productName + '\'' +
                ", productType=" + productType +
                ", productSKUId=" + productSKUId +
                ", productSKUCode='" + productSKUCode + '\'' +
                ", price='" + price + '\'' +
                ", quantity=" + quantity +
                ", pictureBigUrl='" + pictureBigUrl + '\'' +
                ", pictureSmallUrl='" + pictureSmallUrl + '\'' +
                ", pictureMiddleUrl='" + pictureMiddleUrl + '\'' +
                '}';
    }
}
