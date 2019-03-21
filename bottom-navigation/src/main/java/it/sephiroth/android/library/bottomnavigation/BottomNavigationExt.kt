package it.sephiroth.android.library.bottomnavigation

class MenuItemSelectionListenerImpl : BottomNavigation.OnMenuItemSelectionListener {
    private var _menuItemSelect: ((itemId: Int, position: Int, fromUser: Boolean) -> Unit)? = null
    private var _menuItemReselect: ((itemId: Int, position: Int, fromUser: Boolean) -> Unit)? = null

    fun onMenuItemSelect(func: (itemId: Int, position: Int, fromUser: Boolean) -> Unit) {
        _menuItemSelect = func
    }

    fun onMenuItemReselect(func: (itemId: Int, position: Int, fromUser: Boolean) -> Unit) {
        _menuItemReselect = func
    }

    override fun onMenuItemSelect(itemId: Int, position: Int, fromUser: Boolean) {
        _menuItemSelect?.invoke(itemId, position, fromUser)
    }

    override fun onMenuItemReselect(itemId: Int, position: Int, fromUser: Boolean) {
        _menuItemReselect?.invoke(itemId, position, fromUser)
    }

}


fun BottomNavigation.setListener(func: MenuItemSelectionListenerImpl.() -> Unit): BottomNavigation {
    val listener = MenuItemSelectionListenerImpl()
    listener.func()
    menuItemSelectionListener = listener
    return this
}