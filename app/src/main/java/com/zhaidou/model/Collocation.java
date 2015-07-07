package com.zhaidou.model;

/**
 * Created by wangclark on 15/6/24.
 */
public class Collocation {
    private int id;
    private String title;
    private String thumb_pic;
    private String media_pic;
    private String owner_id;
    private String original_author_id;
    private String original_author_nick_name;
    private String competition_id;
    private String author_avatar;
    private String author;
    private String author_email;
    private String author_verified;
    private String nick_name;
    private String author_city;
    private String author_profession;
    private int likes;
    private boolean like_state;
    private int hits;

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

    public String getThumb_pic() {
        return thumb_pic;
    }

    public void setThumb_pic(String thumb_pic) {
        this.thumb_pic = thumb_pic;
    }

    public String getMedia_pic() {
        return media_pic;
    }

    public void setMedia_pic(String media_pic) {
        this.media_pic = media_pic;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public String getOriginal_author_id() {
        return original_author_id;
    }

    public void setOriginal_author_id(String original_author_id) {
        this.original_author_id = original_author_id;
    }

    public String getOriginal_author_nick_name() {
        return original_author_nick_name;
    }

    public void setOriginal_author_nick_name(String original_author_nick_name) {
        this.original_author_nick_name = original_author_nick_name;
    }

    public String getCompetition_id() {
        return competition_id;
    }

    public void setCompetition_id(String competition_id) {
        this.competition_id = competition_id;
    }

    public String getAuthor_avatar() {
        return author_avatar;
    }

    public void setAuthor_avatar(String author_avatar) {
        this.author_avatar = author_avatar;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor_email() {
        return author_email;
    }

    public void setAuthor_email(String author_email) {
        this.author_email = author_email;
    }

    public String getAuthor_verified() {
        return author_verified;
    }

    public void setAuthor_verified(String author_verified) {
        this.author_verified = author_verified;
    }

    public String getNick_name() {
        return nick_name;
    }

    public void setNick_name(String nick_name) {
        this.nick_name = nick_name;
    }

    public String getAuthor_city() {
        return author_city;
    }

    public void setAuthor_city(String author_city) {
        this.author_city = author_city;
    }

    public String getAuthor_profession() {
        return author_profession;
    }

    public void setAuthor_profession(String author_profession) {
        this.author_profession = author_profession;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public boolean isLike_state() {
        return like_state;
    }

    public void setLike_state(boolean like_state) {
        this.like_state = like_state;
    }

    public int getHits() {
        return hits;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    @Override
    public String toString() {
        return "Collocation{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", thumb_pic='" + thumb_pic + '\'' +
                ", media_pic='" + media_pic + '\'' +
                ", owner_id='" + owner_id + '\'' +
                ", original_author_id='" + original_author_id + '\'' +
                ", original_author_nick_name='" + original_author_nick_name + '\'' +
                ", competition_id='" + competition_id + '\'' +
                ", author_avatar='" + author_avatar + '\'' +
                ", author='" + author + '\'' +
                ", author_email='" + author_email + '\'' +
                ", author_verified='" + author_verified + '\'' +
                ", nick_name='" + nick_name + '\'' +
                ", author_city='" + author_city + '\'' +
                ", author_profession='" + author_profession + '\'' +
                ", likes=" + likes +
                ", like_state=" + like_state +
                ", hits=" + hits +
                '}';
    }
}
