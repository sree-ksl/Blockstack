package com.ksl.blockstack

import android.content.Intent
import android.os.Bundle

import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import org.blockstack.android.sdk.BlockstackSession
import org.blockstack.android.sdk.Scope
import org.blockstack.android.sdk.UserData
import org.blockstack.android.sdk.toBlockstackConfig
import java.net.URI


class FirstActivity : AppCompatActivity() {

    private var _blockstackSession: BlockstackSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        signInButton.isEnabled = false

        val scopes = arrayOf(Scope.StoreWrite)
        val config = "https://flamboyant-darwin-d11c17.netlify.com"
                .toBlockstackConfig(scopes)

        _blockstackSession = BlockstackSession(this, config)
        signInButton.isEnabled = true

        signInButton.setOnClickListener { view: View ->
            blockstackSession().redirectUserToSignIn {
                // only called on error
            }
        }
        if (intent?.action == Intent.ACTION_VIEW) {
            handleAuthResponse(intent)
        }
    }

    private fun onSignIn(userData: UserData) {
        userDataTextView.text = "Signed in as ${userData.decentralizedID}"

        signInButton.isEnabled = false
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent?.action == Intent.ACTION_VIEW) {
            handleAuthResponse(intent)
        }
    }

    private fun handleAuthResponse(intent: Intent) {
        val response = intent.dataString
        if (response != null) {
            val authResponseTokens = response.split(':')

            if (authResponseTokens.size > 1) {
                val authResponse = authResponseTokens[1]

                blockstackSession().handlePendingSignIn(authResponse) { userData ->
                    if (userData.hasValue) {
                        // The user is now signed in!
                        runOnUiThread {
                            onSignIn(userData.value!!)
                        }
                    }
                }
            }
        }
    }

    fun blockstackSession() : BlockstackSession {
        val session = _blockstackSession
        if(session != null) {
            return session
        } else {
            throw IllegalStateException("No session.")
        }
    }
}