/*
 * ************************************************************
 * 文件：Theme.java  模块：geeklibrary  项目：MusicPlayer
 * 当前修改时间：2019年01月17日 17:31:47
 * 上次修改时间：2019年01月17日 17:28:59
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.geeklibrary.theme;

@SuppressWarnings("WeakerAccess")
public class Theme {
    private String id;
    private String path;
    private String title;
    private String date;
    private String nav_name;
    private String author;
    private String support_area;
    private String primary_color;
    private String accent_color;
    private String primary_color_dark;
    private String thumbnail;
    private String select;

    public Theme(String id, String path, String title, String date, String nav_name, String author, String support_area, String primary_color, String primary_color_dark, String accent_color, String thumbnail, String select) {
        this.id = id;
        this.path = path;
        this.title = title;
        this.date = date;
        this.nav_name = nav_name;
        this.author = author;
        this.support_area = support_area;
        this.primary_color = primary_color;
        this.thumbnail = thumbnail;
        this.select = select;
        this.accent_color = accent_color;
        this.primary_color_dark = primary_color_dark;
    }

    private Theme(Builder builder) {
        id = builder.id;//1
        path = builder.path;//2
        title = builder.title;//3
        date = builder.date;//4
        nav_name = builder.nav_name;//5
        author = builder.author;//6
        support_area = builder.support_area;//7
        primary_color = builder.primary_color;//8
        primary_color_dark = builder.primary_color_dark;//9
        accent_color = builder.accent_color;//10
        thumbnail = builder.thumbnail;//11
        select = builder.select;//12
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getNav_name() {
        return nav_name;
    }

    public String getAuthor() {
        return author;
    }

    public String getSupport_area() {
        return support_area;
    }

    public String getPrimary_color() {
        return primary_color;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getSelect() {
        return select;
    }

    public String getAccent_color() {
        return accent_color;
    }

    public String getPrimary_color_dark() {
        return primary_color_dark;
    }

    public String getPath() {
        return path;
    }

    public static class Builder {
        private String id;
        private String path;
        private String title;
        private String date;
        private String nav_name;
        private String author;
        private String support_area;
        private String primary_color;
        private String accent_color;
        private String primary_color_dark;
        private String thumbnail;
        private String select;

        public Builder(String id) {
            this.id = id;
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setDate(String s) {
            this.date = s;
            return this;
        }

        public Builder setNavName(String s) {
            this.nav_name = s;
            return this;
        }

        public Builder setAuthor(String s) {
            this.author = s;
            return this;
        }

        public Builder setSupportArea(String s) {
            this.support_area = s;
            return this;
        }

        public Builder setPrimaryColor(String s) {
            this.primary_color = s;
            return this;
        }

        public Builder setPrimaryColorDark(String s) {
            this.primary_color_dark = s;
            return this;
        }

        public Builder setAccentColor(String s) {
            this.accent_color = s;
            return this;
        }

        public Builder setThumbnail(String s) {
            this.thumbnail = s;
            return this;
        }

        public Builder setSelect(String s) {
            this.select = s;
            return this;
        }

        public Theme build() {
            return new Theme(this);
        }
    }
}