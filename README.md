# Pizzeria Order App - RU Hungry?

## Project Overview
The **RU Hungry?** app is an Android-based pizza ordering system that allows users to order New York-style and Chicago-style pizzas. It also provides a customizable pizza-building option where users can select their preferred toppings.

## Key Features
- **Pizza Ordering System**: Users can order specialty pizzas or build their own by selecting toppings.
- **Shopping Cart Functionality**: Add, remove, and modify orders before checkout.
- **UI Components Used**:
  - `RecyclerView` for displaying pizza and topping options
  - `Toast`, `AlertDialog`, `ImageView`, `Spinner`, and `ListView` for user interactions
- **Multi-Activity Structure**: Uses multiple activities for better user experience and navigation.
- **Singleton Design Pattern**: Ensures efficient data management across different activities.

## Source Files
- **MainActivity.java**: Handles the main UI and navigation.
- **OrderActivity.java**: Manages pizza selection and shopping cart interactions.
- **Pizza.java**: Defines the pizza model, including different styles and customizable options.
- **OrderManager.java**: Implements the Singleton pattern to manage orders across activities.

## How to Run
1. Open the project in **Android Studio**.
2. Select **Pixel 3a XL API 34** as the emulator.
3. Build and run the application.
4. Navigate through the app to place an order and test different functionalities.

## Functional Components
- **Order Placement**: Select and customize pizzas from the menu.
- **Shopping Cart Management**: Modify or remove items before checkout.
- **User Interaction**: Various UI components enhance user experience.
- **Error Handling**: Ensures that the app does not crash under any circumstances.

## Testing & Deployment
- Tested on **Pixel 3a XL API 34**.
- Handles exceptions and invalid user inputs gracefully.
- Optimized for smooth navigation and performance.
