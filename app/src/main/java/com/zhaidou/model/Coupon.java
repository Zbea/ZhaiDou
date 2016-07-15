package com.zhaidou.model;

import java.io.Serializable;

/**
 * Created by roy on 16/4/11.优惠券
 */
public class Coupon implements Serializable
{
    public int id;
    /**
     * 优惠券id
     */
    public int couponId;
    /**
     * 优惠券规则id
     */
    public int couponRuleId;
    /**
     * 优惠券编码
     */
    public String couponCode;
    /**
     * 标题
     */
    public String title;
    /**
     * 开始日期
     */
    public String startDate;
    /**
     * 到期日期
     */
    public String endDate;
    /**
     * 剩余时间
     */
    public int time;
    /**
     * 剩余时间
     */
    public String timeStr;
    /**
     * 使用情况
     */
    public String info;
    /**
     * 优惠金额
     */
    public double money;
    /**
     * 满足面额
     */
    public double enoughMoney;
    /**
     *  优惠劵状态:"N", "未使用","U", "已使用","O","已过期"
     */
    public String status;
    /**
     *  优惠劵属性："E":"已兑换" ,"N":"未兑换","S":"禁用"
     */
    public String property;
    /**
     * 优惠券支持商品类型："A"："订单"，"C"："分类"，"D"："单品"
     */
    public String type;
    /**
     * 是否默认
     */
    public boolean isDefault;
    /**
     * 是否不可用
     */
    public boolean isNoUse;
}
