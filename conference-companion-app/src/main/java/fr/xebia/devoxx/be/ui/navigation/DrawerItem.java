package fr.xebia.devoxx.be.ui.navigation;

public class DrawerItem {

    private int mIconRes;
    private int mTextRes;

    public DrawerItem(int iconRes, int textRes) {
        mIconRes = iconRes;
        mTextRes = textRes;
    }

    public int getIconRes() {
        return mIconRes;
    }

    public int getTextRes() {
        return mTextRes;
    }
}
