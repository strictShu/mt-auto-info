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

    public String getShopStar() {
        return shopStar;
    }

    public void setShopStar(String shopStar) {
        this.shopStar = shopStar;
    }

    public String getShopSales() {

        return shopSales;
    }

    public void setShopSales(String shopSales) {
        this.shopSales = shopSales;
    }

    public String getShopPhone() {
        return shopPhone;
    }

    public void setShopPhone(String shopPhone) {
        this.shopPhone = shopPhone;
    }

    @Override
    public String toString() {
        return "ShopInfo{" +
                "shopName='" + shopName + '\'' +
                ", shopAddress='" + shopAddress + '\'' +
                ", shopSales='" + shopSales + '\'' +
                ", shopStar='" + shopStar + '\'' +
                ", shopPhone='" + shopPhone + '\'' +
                '}';
    }
}
