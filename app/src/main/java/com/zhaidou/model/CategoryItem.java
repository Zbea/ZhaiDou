package com.zhaidou.model;

/**
 * Created by wangclark on 15/6/16.
 */
public class CategoryItem {
    /**
     * categoryId : 0201
     * categoryName : 桌面收纳
     * categoryPicUrl : http://imgs.zhaidou.com/saleCate/01/0201/sclog1_20150001.jpg
     * children : null
     * categoryProductCount : null
     */

    public String categoryId;
    public String categoryName;
    public String categoryPicUrl;
    public Object children;
    public Object categoryProductCount;

    private int id;
    private int parentId;
    private int lft;
    private int rgt;
    private String name;
    private String url;
    private String thumb;
    private int level;


    public CategoryItem() {
    }

    public CategoryItem(int id, int parentId, int lft, int rgt, String name, String url, String thumb, int level) {
        this.id = id;
        this.parentId = parentId;
        this.lft = lft;
        this.rgt = rgt;
        this.name = name;
        this.url = url;
        this.thumb = thumb;
        this.level = level;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public int getLft() {
        return lft;
    }

    public void setLft(int lft) {
        this.lft = lft;
    }

    public int getRgt() {
        return rgt;
    }

    public void setRgt(int rgt) {
        this.rgt = rgt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "CategoryItem{" +
                "categoryProductCount=" + categoryProductCount +
                ", categoryId='" + categoryId + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", categoryPicUrl='" + categoryPicUrl + '\'' +
                ", children=" + children +
                '}';
    }
}
