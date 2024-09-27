package database.userData;
import java.util.ArrayList;

public final class DatabaseSearchbar {
    private String type;
    private ArrayList<String> searchResults;
    private String selectedItem;
    private String searchSelectedType;


    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public ArrayList<String> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(final ArrayList<String> searchResults) {
        this.searchResults = searchResults;
    }

    public String getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(final String selectedItem) {
        this.selectedItem = selectedItem;
    }

    public String getSearchSelectedType() {
        return searchSelectedType;
    }

    public void setSearchSelectedType(final String searchSelectedType) {
        this.searchSelectedType = searchSelectedType;
    }
}
