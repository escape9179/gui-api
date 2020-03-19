package logan.guiapi;

import logan.guiapi.fill.FillPlacer;
import logan.guiapi.fill.Filler;
import logan.guiapi.util.PlaceholderParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Tre Logan
 */
public class Menu {

    private static int nextId = 0;

    private final int id;
    private String title;
    private Inventory inventory;
    private int slots;
    private boolean shouldClose = false;

    private Player viewer = null;

    private Map<Integer, MenuItem> menuItems = new HashMap<>();

    public Menu(String title, int rows) {
        id = nextId;
        this.title = title;
        slots = rows * 9;
        nextId++;
    }

    public int getId() {
        return id;
    }

    public void show(Player player) {
        parsePlaceholders(player);
        
        /* Create inventory and add items */
        inventory = Bukkit.createInventory(player, slots, title);
        menuItems.forEach((s, mi) -> inventory.setItem(s, mi.getItemStack()));
        viewer = player;

        GUIAPI.registerMenu(id, this);
        
        player.openInventory(inventory);
    }

    public boolean shouldClose() {
        return shouldClose;
    }

    public void close() {
        shouldClose = true;
        viewer.closeInventory();
    }

    private void parsePlaceholders(Player player) {
        title = PlaceholderParser.parse(title, player);
        menuItems.forEach((s, mi) -> {
            mi.setName(PlaceholderParser.parse(mi.getDisplayName(), player));
            List<String> lore = mi.getLore();
            Stream<String> stream = lore.stream().map(l -> PlaceholderParser.parse(l, player));
            lore = stream.collect(Collectors.toList());
            mi.setLore(lore);
        });
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setRows(int rows) {
        this.slots = rows * 9;
    }

    public void fill(Filler fillPattern) {
        this.fill(fillPattern, Collections.emptyList(), FillPlacer.FillMode.IGNORE);
    }

    public void fill(Filler fillPattern, Collection<Integer> slots, FillPlacer.FillMode mode) {
        fillPattern.fill(this, slots, mode);
    }

    public MenuItem addItem(int slot, MenuItem menuItem) {
        return menuItems.put(slot, menuItem);
    }

    public void removeItem(int slot, MenuItem menuItem) {
        menuItems.remove(slot);
    }

    public Map<Integer, MenuItem> getMenuItems() {
        return menuItems;
    }

    public String getTitle() {
        return title;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public int getSlots() {
        return slots;
    }

    public int getTopLeft() {
        return 0;
    }

    public int getTopRight() {
        return 8;
    }

    public int getBottomLeft() {
        return slots - 9;
    }

    public int getBottomRight() {
        return slots - 1;
    }

    public Player getViewer() {
        return viewer;
    }

    public void onInventoryClick(InventoryClickEvent event) {

        if (!(viewer.getUniqueId()).equals(event.getWhoClicked().getUniqueId())) return;

        event.setCancelled(true);

        System.out.println("Got to here without an exception 1");

        menuItems.keySet().stream()
                .filter(s -> s == event.getSlot())
                .forEach(s -> menuItems.get(s).onClick(new MenuItemClickEvent(event)));
    }
}
