package com.condecosoftware.core

import android.content.ComponentName
import android.net.Uri
import android.support.customtabs.CustomTabsClient
import android.support.customtabs.CustomTabsIntent
import android.support.customtabs.CustomTabsServiceConnection
import android.support.customtabs.CustomTabsSession

private const val CHROME_PACKAGE_NAME = "com.android.chrome"

/**
 * Custom chrome tabs wrapper to keep the states.
 */
class CustomChromeTabs(val context: android.content.Context) {
    private var customTabsClient: CustomTabsClient? = null
    private var customTabsServiceConnection: CustomTabsServiceConnection? = null
    private var customTabsSession: CustomTabsSession? = null

    /**
     * Called to bind to custom tabs service
     */
    fun bindCustomTabsService() {
        var connection = customTabsServiceConnection
        if (connection != null)
            return

        connection = object : CustomTabsServiceConnection() {

            override fun onCustomTabsServiceConnected(componentName: ComponentName, client: CustomTabsClient) {
                customTabsClient = client
                customTabsSession = client.newSession(null)
                client.warmup(0)
            }

            override fun onServiceDisconnected(componentName: ComponentName) {
                customTabsClient = null
                customTabsSession = null
            }
        }

        if (CustomTabsClient.bindCustomTabsService(context, CHROME_PACKAGE_NAME, connection)) {
            this.customTabsServiceConnection = connection
        } else {
            context.unbindService(connection)
        }
    }

    /**
     * Called to clean up custom tabs binding
     */
    fun unbindCustomTabsService() {
        val connection = customTabsServiceConnection
        customTabsServiceConnection = null
        if (connection != null) {
            context.unbindService(connection)
        }
    }

    /**
     * Call to create a builder for customising chrome tabs before launching uri.
     */
    fun createTabBuilder(uri: Uri): CustomTabsIntent.Builder? {
        return customTabsSession?.let {
            it.mayLaunchUrl(uri, null, null)
            CustomTabsIntent.Builder(it)
        }
    }
}