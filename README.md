# BuyAndSell Plugin
Minecraft server plugin that lets users buy and sell items.

## Commands:

- /buy item quantity
  - Purchases item(s) of the specified type and quantity.
  
- /sell item|hand|this (quantity|all)
  - Sells item(s) of the specified type (or hand item if not specified) and quantity.

- /sell all
  - Sells all sellable items in a player's inventory.

- /price item
  - Retrieves the price of the specified item.
  
- /pricelist (page #)
  - Prints a list of buyable/sellable items and their prices.
  
- /setprice (item|hand) buyprice sellprice
  - Sets the buy and sell prices of the specified item to the specified price.

- removeprice (item|hand)
  - Removes the specified price from the pricelist.

- /setbuyprice (item|hand) price
  - Sets the price to buy the specified item to the specified price.
  
- /setsellprice (item|hand) price
  - Sets the price paid to the player when selling the specified item.

*Note: Parenthesized command parameters are optional.*

## Installing:

Simply drag and drop the [BuyAndSell.jar file](https://github.com/daviddecorso/BuyAndSell/blob/master/target/BuyAndSell-1.0.jar) into your server's plugin folder.

The plugin will create a new directory and prices file to store item prices. You can start adding prices via the /setbuyprice and /setsellprice commands, or directly into the prices.txt file with the format (Item) (Buy price) (Sell price).

An example line of the file would look like this:
`ender_pearl 50.0 15.0`
  
## Dependencies:

Needs an updated version of the [Vault plugin](https://github.com/MilkBowl/Vault) to run.

Works with any economy plugins supported by Vault.

## Permissions:

- BuyAndSell.setPrices
  - Determines if a player can set prices for items.
  
- BuyAndSell.checkPrices
  - Determines if a player can check the price of an item.
  
- BuyAndSell.viewPriceList
  - Determines if a player can check the price of an item.
  
- BuyAndSell.buyItems
  - Determines if a player can buy items.
  
- BuyAndSell.sellItems
  - Determins if a player can sell items.
  
## To-do:

- [x] Add /remove command

- [x] Make a command that sets the sell and buy prices simultaneously: /setprices sellprice buyprice

- [x] Make a command that prints a list of buyable items

- [x] Add "all" keyword

- [x] Add /price command

- [x] Sort and remove duplicates from prices file


*Copyright (C) 2020 by David DeCorso. Software is free to use and distribute.*
