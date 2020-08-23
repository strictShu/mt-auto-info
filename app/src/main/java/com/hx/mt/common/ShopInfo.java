package com.hx.mt.common;

/**
 * shop_info
 */
public class ShopInfo {
    private String shopName;
    private String shopAddress;
    private String shopSales;
    private String shopStar;
    private String shopPhone;
    private String shopAreaId;
    private String shopSearchAddress;

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopAddress() {
        return shopAddress;
    }

    public void setShopAddress(String shopAddress) {
        this.shopAddress = shopAddress;
    }

    public String getShopSales() {
        return shopSales;
    }

    public void setShopSales(String shopSales) {
        this.shopSales = shopSales;
    }

    public String getShopStar() {
        return shopStar;
    }

    public void setShopStar(String shopStar) {
        this.shopStar = shopStar;
    }

    public String getShopPhone() {
        return shopPhone;
    }

    public void setShopPhone(String shopPhone) {
        this.shopPhone = shopPhone;
    }

    public String getShopAreaId() {
        return shopAreaId;
    }

    public void setShopAreaId(String shopAreaId) {
        this.shopAreaId = shopAreaId;
    }

    public String getShopSearchAddress() {
        return shopSearchAddress;
    }

    public void setShopSearchAddress(String shopSearchAddress) {
        this.shopSearchAddress = shopSearchAddress;
    }

    @Override
    public String toString() {
        return "ShopInfo{" +
                "shopName='" + shopName + '\'' +
                ", shopAddress='" + shopAddress + '\'' +
                ", shopSales='" + shopSales + '\'' +
                ", shopStar='" + shopStar + '\'' +
                ", shopPhone='" + shopPhone + '\'' +
                ", shopAreaId='" + shopAreaId + '\'' +
                ", shopSearchAddress='" + shopSearchAddress + '\'' +
                '}';
    }
}
