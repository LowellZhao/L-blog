package com.lwz.dto;

public enum Types {
    TAG("tag"),                 // 标签
    CATEGORY("category"),       // 文章分类
    ARTICLE("post"),            // 内容类型是文章
    PUBLISH("publish"),         // 公开
    PAGE("page"),               // 内容类型是页面
    DRAFT("draft"),             // 草稿
    LINK("link"),               // 连接
    IMAGE("image"),             // 图片
    FILE("file"),               // 文件
    CSRF_TOKEN("csrf_token"),
    COMMENTS_FREQUENCY("comments:frequency"),
    HITS_FREQUENCY("hits:frequency"),

    /**
     * 附件存放的URL，默认为网站地址，如集成第三方则为第三方CDN域名
     */
    ATTACH_URL("attach_url"),

    /**
     * 网站要过滤，禁止访问的ip列表
     */
    BLOCK_IPS("site_block_ips");


    private String type;

    public java.lang.String getType() {
        return type;
    }

    public void setType(java.lang.String type) {
        this.type = type;
    }

    Types(java.lang.String type) {
        this.type = type;
    }
}
