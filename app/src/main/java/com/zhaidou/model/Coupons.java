package com.zhaidou.model;
import java.util.List;


/**
 * User: Scoield(553899626@qq.com)
 * Date: 2016-04-18
 * Time: 14:45
 * Description:优惠卷
 * FIXME
 */
public class Coupons {

    /**
     * id : 2
     * couponId : 6
     * couponRuleId : 5
     * couponCode : LPSA2MKK8SFN2JS4
     * couponName : test
     * enoughValue : 130
     * bookValue : 10
     * startTime : 2016-03-21 20:00:00
     * endTime : 2016-04-21 20:00:00
     * usedTime :
     * orderCode :
     * userId : 1
     * userName : donnie0915
     * status : O
     * property : E
     * gmtCreated : 2016-04-01 17:58:01
     * gmtModified : 2016-04-06 14:11:52
     * goodsType : C
     * couponGoodsTypeNames : ["桌面收纳","其他收纳","灯饰","椅子"]
     */

    public int id;
    public int couponId;
    public int couponRuleId;
    public String couponCode;
    public String couponName;
    public int enoughValue;
    public int bookValue;
    public String startTime;
    public String endTime;
    public String usedTime;
    public String orderCode;
    public int userId;
    public String userName;
    public String status;
    public String property;
    public String gmtCreated;
    public String gmtModified;
    public String goodsType;
    public List<String> couponGoodsTypeNames;

}
