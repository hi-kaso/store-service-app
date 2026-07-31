import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

// メニュー情報を一元管理するクラス（Singletonパターン）

public class MenuRepository {
    private static MenuRepository instance;
    private final Map<String, Integer> menuItems;

    private MenuRepository() {
        menuItems = new LinkedHashMap<>();
        menuItems.put("ハンバーガー", 300);
        menuItems.put("チーズバーガー", 350);
        menuItems.put("ポテト(M)", 200);
        menuItems.put("ドリンク(M)", 150);
    }

    public static synchronized MenuRepository getInstance() {
        if (instance == null) {
            instance = new MenuRepository();
        }
        return instance;
    }

    public Map<String, Integer> getMenuItems() {
        return Collections.unmodifiableMap(menuItems);
    }
}