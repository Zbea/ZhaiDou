package com.zhaidou.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangclark on 15/6/12.
 */
public class SwitchImage {
    private String url;
    private int id;
    private String title;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public SwitchImage(String url, int id, String title) {
        this.url = url;
        this.id = id;
        this.title = title;
    }

    public SwitchImage() {
    }

    public static List<SwitchImage> getTestData(){
        String[] titles={"飞机","地球","美景"};
        String[] urls={"https://ss1.baidu.com/9vo3dSag_xI4khGko9WTAnF6hhy/super/whfpf%3D425%2C260%2C50/sign=b5098d89319b033b2cddaf9a73f302e1/3812b31bb051f819e73b93c7dfb44aed2e73e725.jpg",
                       "http://b.hiphotos.baidu.com/image/w%3D310/sign=8518207ff31f3a295ac8d3cfa924bce3/6609c93d70cf3bc796714c4dd200baa1cd112a19.jpg",
                       "http://a.hiphotos.baidu.com/image/w%3D310/sign=5bcfc07441a98226b8c12d26ba82b97a/f3d3572c11dfa9ec3a18141660d0f703918fc1f9.jpg"};
        List<SwitchImage> switchImageList =new ArrayList<SwitchImage>();
        for(int i=0;i<3;i++){
            SwitchImage switchImage = new SwitchImage(urls[i],i,titles[i]);
            switchImageList.add(switchImage);
        }
        return  switchImageList;
    }
}
