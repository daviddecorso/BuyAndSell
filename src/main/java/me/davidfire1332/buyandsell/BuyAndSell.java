/********************************************
             BUYANDSELL PLUGIN:

 This plugin lets users buy and sell items.
      (More information in the readme)

 Copyright (C) 2020 - David DeCorso
 This plugin is free to use and distribute.
 *******************************************/

package me.davidfire1332.buyandsell;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import static org.bukkit.Material.matchMaterial;

public final class BuyAndSell extends JavaPlugin {

    private static final Logger log = Logger.getLogger("Minecraft");
    HashMap itemPrices = new HashMap();
    File pricesList = null;
    ArrayList<String> pricesToWrite = new ArrayList<>();
    Economy econ;

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        String itemFromPriceFile, tempString;

        // Loads economy
        if (!setupEconomy() ) {
            log.severe(String.format("§4Error: [%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // loads the prices file or creates one if it doesn't exist.
        pricesList = new File("plugins/buyAndSell/prices.txt");

        if (!pricesList.isFile())
        {
            new File("plugins/buyAndSell").mkdirs();
            try {
                pricesList.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Scanner s = null;
        double sellPriceFromPriceFile, buyPriceFromPriceFile;
        Pair pricesFromPriceFile;
        try {
            s = new Scanner(pricesList);
        } catch (FileNotFoundException e) {
            System.out.println("§4Error: Couldn't find prices.txt");
            e.printStackTrace();
        }

        // Reads the prices file into a hash map storing the items and prices.
        // Buy and sell prices are stored as a pair and that pair is associated with the item name.
        if (s == null)
        {
            System.out.println("§4Error: unable to find file for price list. File must be named prices.txt.");
        }
        else
        {
            while (s.hasNextLine())
            {
                if (s.hasNext())
                {
                    itemFromPriceFile = s.next();
                    if (s.hasNextDouble())
                    {
                        buyPriceFromPriceFile = s.nextDouble();
                    }
                    else
                    {
                        break;
                    }
                    if (s.hasNextDouble())
                    {
                        sellPriceFromPriceFile = s.nextDouble();
                    }
                    else
                    {
                        sellPriceFromPriceFile = 0.0;
                    }
                    pricesFromPriceFile = new Pair(buyPriceFromPriceFile, sellPriceFromPriceFile);
                    if (!itemPrices.containsKey(itemFromPriceFile))
                    {
                        pricesToWrite.add(itemFromPriceFile + " " + buyPriceFromPriceFile + " " + sellPriceFromPriceFile);
                    }
                    else
                    {
                        Pair tempPair = (Pair) itemPrices.get(itemFromPriceFile);
                        tempString = itemFromPriceFile + " " + tempPair.getBuyPrice() + " " + tempPair.getSellPrice();
                        pricesToWrite.remove(tempString);
                        pricesToWrite.add(itemFromPriceFile + " " + buyPriceFromPriceFile + " " + sellPriceFromPriceFile);
                    }
                    itemPrices.put(itemFromPriceFile, pricesFromPriceFile);
                }
                else
                {
                    break;
                }
            }
            System.out.println("§a[BuyAndSell] Loaded prices.");
            s.close();
            pricesList = null;
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        // On shutdown all of the prices are sorted and written to a file.
        pricesList = new File("plugins\\buyAndSell\\prices.txt");
        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;
        try {
            fw = new FileWriter(pricesList, false);
            bw = new BufferedWriter(fw);
            pw = new PrintWriter(bw);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (pw == null)
        {
            System.out.println("§4Error: Couldn't write prices to file!");
            return;
        }

        pricesToWrite.sort(Comparator.comparing(String::toString));

        for (int i = 0; i < pricesToWrite.size(); i++)
        {
            pw.println(pricesToWrite.get(i));
        }

        pw.close();
        try {
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (command.getName().equals("buy"))
        {
            if (sender instanceof Player)
            {
                Material playerBuyMaterial;

                Player player = (Player) sender;
                OfflinePlayer offlinePlayer = (OfflinePlayer) sender;
                double itemPrice;
                int quantity;
                Pair pairPrice;

                if (args != null && args.length > 0 &&!args[0].equals(""))
                {
                    String materialString = args[0];
                    playerBuyMaterial = matchMaterial(materialString);

                }
                else
                {
                    return false;
                }

                if (args.length != 2)
                {
                    return false;
                }
                else if (args[0].length() > 100)
                {
                    player.sendMessage("§4Error: Invalid item.");
                    return false;
                }
                else if (args[1].length() > 8)
                {
                    player.sendMessage("§4Error: You are trying to buy too many items.");
                    return false;
                }
                else if (playerBuyMaterial == null)
                {
                    player.sendMessage("§4Error: \""+ args[0] + "\"" + " is not a valid item.");
                    return false;
                }
                else if(!playerBuyMaterial.isItem())
                {
                    player.sendMessage("§4You cannot purchase " + "\""+ args[0] + "\"" + ".");
                    return false;
                }
                else if (itemPrices.isEmpty())
                {
                    System.out.println("§4Error: Price list is empty!");
                    player.sendMessage("§4Error: This plugin has been configured incorrectly. Please contact a server administrator.");
                    return false;
                }

                if (isNumeric(args[1]))
                {
                    if (Double.parseDouble(args[1]) > 2304)
                    {
                        player.sendMessage("§4You are trying to buy too many items.");
                        return false;
                    }
                }
                else
                {
                    if (!args[1].equals("all"))
                        return false;
                }

                if (itemPrices.containsKey(args[0]))
                {
                    pairPrice = (Pair) itemPrices.get(args[0]);
                    itemPrice = (double) pairPrice.getBuyPrice();
                }
                else
                {
                    player.sendMessage("§4You cannot buy this item.");
                    return false;
                }

                quantity = Integer.parseInt(args[1]);

                if (econ.has(offlinePlayer, itemPrice * quantity))
                {
                    ItemStack items = new ItemStack(playerBuyMaterial, quantity);
                    player.getInventory().addItem(items);
                    econ.withdrawPlayer(offlinePlayer, itemPrice * quantity);
                    player.sendMessage("§aYou just bought " + args[1] + " " + args[0] + " for $" + (itemPrice * quantity) + ".");
                }
                else
                {
                    player.sendMessage("§4You do not have enough money to make this purchase.");
                }
            }
            else
            {
                System.out.println("§4Error: A player must run this command.");
            }

            return true;
        }

        else if (command.getName().equals("sell"))
        {
            Material playerBuyMaterial;
            Player player = (Player) sender;
            OfflinePlayer offlinePlayer = (OfflinePlayer) sender;
            String materialString;
            double itemPrice;
            int quantity = 0, totalQuantity = 0, totalSum = 0;
            Pair pairPrice;
            ItemStack items, tempStack;

            if (args.length == 1)
            {
                items = player.getInventory().getItemInMainHand();
                playerBuyMaterial = items.getType();
                materialString = playerBuyMaterial.toString().toLowerCase();

                if (args[0].equals("all"))
                {
                    Object[] itemArray = player.getInventory().getContents();
                    for (int i = 0; i < itemArray.length; i++)
                    {
                        tempStack = (ItemStack) itemArray[i];
                        if (tempStack == null)
                        {
                            continue;
                        }
                        playerBuyMaterial = tempStack.getType();
                        materialString = playerBuyMaterial.toString().toLowerCase();
                        if (itemPrices.containsKey(materialString))
                        {
                            quantity = tempStack.getAmount();
                            totalQuantity += quantity;
                            pairPrice = (Pair) itemPrices.get(materialString);
                            itemPrice = (double) pairPrice.getSellPrice();

                            player.getInventory().removeItem(tempStack);
                            econ.depositPlayer(offlinePlayer, itemPrice * quantity);
                            totalSum += (itemPrice * quantity);
                        }
                    }
                    player.sendMessage("§aYou just sold " + totalQuantity + " item(s) for $" + totalSum + ".");
                    return true;
                }
                else
                {
                    if (!isNumeric(args[0]))
                    {
                        return false;
                    }
                    quantity = Integer.parseInt(args[0]);
                }
                items = new ItemStack(playerBuyMaterial, quantity);
            }
            else if (args.length == 2)
            {
                if (args[0].equals("hand") || args[0].equals("this"))
                {
                    items = player.getInventory().getItemInMainHand();
                    playerBuyMaterial = items.getType();
                    materialString = playerBuyMaterial.toString().toLowerCase();
                }
                else
                {
                    materialString = args[0];
                    playerBuyMaterial = matchMaterial(materialString);
                }
                if (playerBuyMaterial == null)
                {
                    player.sendMessage("§4\""+ args[0] + "\"" + " is not a valid item.");
                    return false;
                }
                if (args[1].equals("all"))
                {
                    HashMap itemMap = player.getInventory().all(playerBuyMaterial);
                    Collection itemCollection = itemMap.values();
                    Object[] itemArray = itemCollection.toArray();
                    for (int i = 0; i < itemCollection.size(); i++)
                    {
                        tempStack = (ItemStack) itemArray[i];
                        quantity += tempStack.getAmount();
                    }
                }
                else
                {
                    if (args[1].length() > 8)
                    {
                        player.sendMessage("§4Error: You are trying to sell too many items.");
                        return false;
                    }
                    if (Double.parseDouble(args[1]) > 2304)
                    {
                        player.sendMessage("§4Error: You are trying to sell too many items.");
                        return false;
                    }
                    if (args[0].length() > 100)
                    {
                        player.sendMessage("§4Error: Invalid item.");
                        return false;
                    }
                    if(!playerBuyMaterial.isItem()) {
                        player.sendMessage("§4You cannot sell " + "\""+ args[0] + "\"" + ".");
                        return true;
                    }
                    quantity = Integer.parseInt(args[1]);
                }

                items = new ItemStack(playerBuyMaterial, quantity);
            }
            else
            {
                return false;
            }

            if (itemPrices.isEmpty())
            {
                System.out.println("§4[BuyAndSell] Error: Price list is empty!");
                player.sendMessage("§4Error: This plugin has been configured incorrectly. Please contact a server administrator.");
                return true;
            }
            else
            {
                if (player.getInventory().containsAtLeast(items, quantity))
                {
                    if (itemPrices.containsKey(materialString))
                    {
                        pairPrice = (Pair) itemPrices.get(materialString);
                        itemPrice = (double) pairPrice.getSellPrice();
                    }
                    else
                    {
                        player.sendMessage("§4You cannot sell this item.");
                        return true;
                    }
                    player.getInventory().removeItem(items);
                    econ.depositPlayer(offlinePlayer, itemPrice * quantity);
                    player.sendMessage("§aYou just sold " + quantity + " " + materialString + " for $" + (itemPrice * quantity) + ".");
                }
                else
                {
                    player.sendMessage("§4You don't have enough items of that type to sell.");
                }
                return true;
            }
        }

        else if (command.getName().equals("setprice"))
        {
            String materialString;
            Material playerBuyMaterial;
            Player player = (Player) sender;
            ItemStack items;
            double buyPrice, sellPrice;

            if (args == null || args.length > 3 || args.length < 2)
            {
                return false;
            }
            else if (args.length == 2)
            {
                items = player.getInventory().getItemInMainHand();
                playerBuyMaterial = items.getType();
                materialString = playerBuyMaterial.toString().toLowerCase();

                if (!isNumeric(args[0]) || !isNumeric(args[1]))
                {
                    return false;
                }

                if (args[0].length() > 10 || args[1].length() > 10)
                {
                    player.sendMessage("§4Error: That price is too expensive.");
                    return false;
                }

                buyPrice = Double.parseDouble(args[0]);
                sellPrice = Double.parseDouble(args[1]);
            }
            else
            {
                materialString = args[0];
                playerBuyMaterial = matchMaterial(materialString);
                materialString = playerBuyMaterial.toString().toLowerCase();
                if (args[0].length() > 100)
                {
                    player.sendMessage("§4Error: Invalid item.");
                    return false;
                }
                else if (args[1].length() > 8)
                {
                    player.sendMessage("§4Error: That price is too expensive.");
                    return false;
                }

                buyPrice = Double.parseDouble(args[1]);
                sellPrice = Double.parseDouble(args[2]);
            }

            Pair pricePair = (Pair) itemPrices.get(materialString);

            if (pricePair == null)
            {
                pricePair = new Pair(buyPrice, sellPrice);
            }
            else
            {
                if (!itemPrices.containsKey(materialString))
                {
                    pricesToWrite.add(materialString + " " + pricePair.getBuyPrice() + " " + pricePair.getSellPrice());
                }
                else
                {
                    Pair tempPair = (Pair) itemPrices.get(materialString);
                    String tempString = materialString + " " + 0.0 + " " + tempPair.getSellPrice();
                    pricesToWrite.remove(tempString);
                    tempString = materialString + " " + tempPair.getBuyPrice() + " " + 0.0;
                    pricesToWrite.remove(tempString);
                    tempString = materialString + " " + tempPair.getBuyPrice() + " " + tempPair.getSellPrice();
                    pricesToWrite.remove(tempString);
                }
                pricePair.setPrices(buyPrice, sellPrice);
            }

            pricesToWrite.add(materialString + " " + pricePair.getBuyPrice() + " " + pricePair.getSellPrice());
            itemPrices.put(materialString, pricePair);

            player.sendMessage("§aSuccessfully set the buy price of " + materialString + " to $" + buyPrice + ", and the sell price to $" + sellPrice + ".");
            return true;

        }

        else if (command.getName().equals("setbuyprice"))
        {
            String materialString;
            Material playerBuyMaterial;
            Player player = (Player) sender;
            ItemStack items;
            double price;

            if (args != null && args.length == 2 && !args[0].equals(""))
            {
                materialString = args[0];
                playerBuyMaterial = matchMaterial(materialString);

                if (args[0].length() > 100)
                {
                    player.sendMessage("§4Error: Invalid item.");
                    return false;
                }
                else if (args[1].length() > 8)
                {
                    player.sendMessage("§4Error: That price is too expensive.");
                    return false;
                }
                if (isNumeric(args[1]))
                {
                    if (Double.parseDouble(args[1]) > 10000000)
                    {
                        player.sendMessage("§4Error: That price is too expensive.");
                        return false;
                    }
                }
                else
                {
                    return false;
                }
                if (playerBuyMaterial == null) {
                    player.sendMessage("§4Error: \""+ args[0] + "\"" + " is not a valid item.");
                    return false;
                }

                price = Double.parseDouble(args[1]);
            }
            else if (args.length == 1)
            {
                items = player.getInventory().getItemInMainHand();
                playerBuyMaterial = items.getType();
                materialString = playerBuyMaterial.toString().toLowerCase();
                price = Double.parseDouble(args[0]);
            }
            else
            {
                return false;
            }

            Pair pricePair = (Pair) itemPrices.get(materialString);

            if (pricePair == null)
            {
                pricePair = new Pair(price, 0.0);
            }
            else
            {
                if (!itemPrices.containsKey(materialString))
                {
                    pricesToWrite.add(materialString + " " + pricePair.getBuyPrice() + " " + pricePair.getSellPrice());
                }
                else
                {
                    Pair tempPair = (Pair) itemPrices.get(materialString);
                    String tempString = materialString + " " + 0.0 + " " + tempPair.getSellPrice();
                    pricesToWrite.remove(tempString);
                    tempString = materialString + " " + tempPair.getBuyPrice() + " " + 0.0;
                    pricesToWrite.remove(tempString);
                    tempString = materialString + " " + tempPair.getBuyPrice() + " " + tempPair.getSellPrice();
                    pricesToWrite.remove(tempString);
                }
                pricePair.setBuyPrice(price);
            }


            pricesToWrite.add(materialString + " " + pricePair.getBuyPrice() + " " + pricePair.getSellPrice());
            itemPrices.put(materialString, pricePair);

            player.sendMessage("§aSuccessfully set the price of " + materialString + " to $" + price + ".");
            return true;
        }
        else if (command.getName().equals("setsellprice"))
        {
            String materialString;
            Player player = (Player) sender;
            Material playerBuyMaterial;
            ItemStack items;
            double price;

            if (args != null && args.length == 2 &&!args[0].equals(""))
            {
                materialString = args[0];
                playerBuyMaterial = matchMaterial(materialString);

                if (args[0].length() > 100)
                {
                    player.sendMessage("§4Error: Invalid item.");
                    return false;
                }
                else if (args[1].length() > 8)
                {
                    player.sendMessage("§4Error: That price is too expensive.");
                    return false;
                }
                if (isNumeric(args[1]))
                {
                    if (Double.parseDouble(args[1]) > 10000000)
                    {
                        player.sendMessage("§4Error: That price is too expensive.");
                        return false;
                    }
                }
                else
                {
                    return false;
                }
                if (playerBuyMaterial == null) {
                    player.sendMessage("§4Error: \""+ args[0] + "\"" + " is not a valid item.");
                    return false;
                }
                price = Double.parseDouble(args[1]);
            }
            else if (args.length == 1)
            {
                items = player.getInventory().getItemInMainHand();
                playerBuyMaterial = items.getType();
                materialString = playerBuyMaterial.toString().toLowerCase();
                price = Double.parseDouble(args[0]);
            }
            else
            {
                return false;
            }

            Pair pricePair = (Pair) itemPrices.get(materialString);

            if (pricePair == null)
            {
                pricePair = new Pair(0.0, price);
            }
            else
            {
                if (!itemPrices.containsKey(materialString))
                {
                    pricesToWrite.add(materialString + " " + pricePair.getBuyPrice() + " " + pricePair.getSellPrice());
                }
                else
                {
                    Pair tempPair = (Pair) itemPrices.get(materialString);
                    String tempString = materialString + " " + tempPair.getBuyPrice() + " " + 0.0;
                    pricesToWrite.remove(tempString);
                    tempString = materialString + " " + 0.0 + " " + tempPair.getSellPrice();
                    pricesToWrite.remove(tempString);
                    tempString = materialString + " " + tempPair.getBuyPrice() + " " + tempPair.getSellPrice();
                    pricesToWrite.remove(tempString);
                }
                pricePair.setSellPrice(price);
            }

            pricesToWrite.add(materialString + " " + pricePair.getBuyPrice() + " " + pricePair.getSellPrice());
            itemPrices.put(materialString, pricePair);

            player.sendMessage("§aSuccessfully set the price of " + materialString + " to $" + price + ".");
            return true;
        }

        else if (command.getName().equals("price"))
        {
            Material playerBuyMaterial;
            Player player = (Player) sender;
            String materialString;
            Pair pairPrice;
            ItemStack items;

            if (args != null && args.length == 1 && !args[0].equals(""))
            {
                if (args[0].length() > 100)
                {
                    player.sendMessage("§4Error: Invalid item.");
                    return false;
                }

                materialString = args[0];
                playerBuyMaterial = matchMaterial(materialString);

                if (playerBuyMaterial == null)
                {
                    player.sendMessage("§4Error: \""+ args[0] + "\"" + " is not a valid item.");
                    return false;
                }
                materialString = playerBuyMaterial.toString().toLowerCase();
            }
            else if (args.length == 0) {
                items = player.getInventory().getItemInMainHand();
                playerBuyMaterial = items.getType();
                materialString = playerBuyMaterial.toString().toLowerCase();
            }
            else
            {
                return false;
            }

            if (itemPrices.containsKey(materialString))
            {
                pairPrice = (Pair) itemPrices.get(materialString);
                player.sendMessage("§a" + materialString + " costs $" + pairPrice.getBuyPrice() + " to buy and pays $" + pairPrice.getSellPrice() + " when sold.");
                return true;
            }
            else
            {
                player.sendMessage("§4You cannot buy that item.");
                return true;
            }
        }

        else if (command.getName().equals("removeprice") || command.getName().equals("removeprices"))
        {
            Material playerBuyMaterial;
            Player player = (Player) sender;
            String materialString;
            Pair pairPrice;
            ItemStack items;
            String writeString;

            if (args != null && args.length == 1 && !args[0].equals(""))
            {
                if (args[0].length() > 100)
                {
                    player.sendMessage("§4Error: Invalid item.");
                    return false;
                }

                materialString = args[0];
                playerBuyMaterial = matchMaterial(materialString);

                if (playerBuyMaterial == null)
                {
                    player.sendMessage("§4Error: \""+ args[0] + "\"" + " is not a valid item.");
                    return false;
                }
                materialString = playerBuyMaterial.toString().toLowerCase();
            }
            else if (args.length == 0) {
                items = player.getInventory().getItemInMainHand();
                playerBuyMaterial = items.getType();
                materialString = playerBuyMaterial.toString().toLowerCase();
            }
            else
            {
                return false;
            }

            if (itemPrices.containsKey(materialString))
            {
                pairPrice = (Pair) itemPrices.get(materialString);
                writeString = materialString + " " + pairPrice.getBuyPrice() + " " + pairPrice.getSellPrice();
                pricesToWrite.remove(writeString);
                itemPrices.remove(materialString);
                player.sendMessage("§a" + materialString + " has been removed from the price list.");
                return true;
            }
            else
            {
                player.sendMessage("§4Item not in price list.");
                return true;
            }
        }

        else if (command.getName().equals("pricelist") || command.getName().equals("priceslist"))
        {
            int page, i, priceListSize, lineCount = 0, totalPages;

            int lineLength = 8;

            if (args.length > 1)
            {
                return false;
            }
            if (args.length == 0)
            {
                page = 1;
            }
            else
            {
                if (args[0].length() > 100000||!isNumeric(args[0]))
                {
                    return false;
                }
                page = Integer.parseInt(args[0]);
            }
            Player player = (Player) sender;
            priceListSize = pricesToWrite.size();
            totalPages = priceListSize / 9;

            if (page > totalPages + 1)
            {
                return true;
            }

            pricesToWrite.sort(Comparator.comparing(String::toString));

            player.sendMessage("§b---- §aPRICE LIST §b----- §aPage §c" + page + "§a/§c" + (totalPages + 1) + "§b----");
            player.sendMessage("§b---- §aItem §b| §aBuy Price §b| §aSell Price §b----");
            for (i = (page - 1) * lineLength; i < priceListSize; i++)
            {
                player.sendMessage("§a" + pricesToWrite.get(i).replace(" ", " $"));
                lineCount++;
                if (lineCount > lineLength)
                {
                    break;
                }
            }
            if (i < pricesToWrite.size())
            {
                if (page <= totalPages)
                {
                    player.sendMessage("§aType §c/" + command.getName() + " " + (page + 1) + "§a to read the next page.");
                }

            }
            return true;
        }

        return false;
    }
}

class Pair<K, V> {

    private K buyPrice;
    private V sellPrice;

    public static <K, V> Pair<K, V> createPair(K buyPrice, V sellPrice)
    {
        return new Pair<K, V>(buyPrice, sellPrice);
    }

    public Pair(K buyPrice, V sellPrice)
    {
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    public void setSellPrice(V sellPrice)
    {
        this.sellPrice = sellPrice;
    }

    public void setBuyPrice(K buyPrice)
    {
        this.buyPrice = buyPrice;
    }

    public void setPrices(K buyPrice, V sellPrice)
    {
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    public K getBuyPrice()
    {
        return buyPrice;
    }

    public V getSellPrice()
    {
        return sellPrice;
    }

    public String pairToString()
    {
        return buyPrice + " " + sellPrice;
    }

}