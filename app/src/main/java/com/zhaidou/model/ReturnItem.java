package com.zhaidou.model;

/**
  * User: Scoield(553899626@qq.com)
  * Date: 2015-12-15
  * Time: 16:06
  * Description:退货实体类
  * FIXME
  */
 public class ReturnItem {

    public String orderItemId;
    public int quantity;
    public String remark;

    public ReturnItem() {
    }

    public ReturnItem(String orderItemId, int quantity, String remark) {
        this.orderItemId = orderItemId;
        this.quantity = quantity;
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "ReturnItem{" +
                "orderItemId='" + orderItemId + '\'' +
                ", quantity=" + quantity +
                ", remark='" + remark + '\'' +
                '}';
    }
}
