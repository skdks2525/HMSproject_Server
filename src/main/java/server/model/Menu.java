package server.model;

public class Menu {
    private String menuId;
    private String name;
    private int price;
    private String category;
    private boolean isAvailable;
    private int stock;

    public Menu(String menuid, String name, int price, String category, boolean isAvailable, int stock) {
        this.menuId = menuid;
        this.name = name;
        this.price = price;
        this.category = category;
        this.isAvailable = isAvailable;
        this.stock = stock;
    }

    public String getMenuId() {
        return menuId;
    }
    public String getName() {
        return name;
    }
    public int getPrice() {
        return price;
    }
    public String getCategory() {
        return category;
    }
    public boolean getIsAvailable() {
        return isAvailable;
    }
    public int getStock() {
        return stock;
    }

    public void setMenuId(String menuid) {
        this.menuId = menuid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setIsAvailable(boolean isavailable) {
        this.isAvailable = isavailable;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
