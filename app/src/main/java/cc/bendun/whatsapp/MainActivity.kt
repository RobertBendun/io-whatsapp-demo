package cc.bendun.whatsapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

const val WhatsAppMimeType = "vnd.android.cursor.item/vnd.com.whatsapp.voip.call"

class MainActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		// get all UI components by their ID
		val contact      = findViewById<EditText>(R.id.contact)
		val button       = findViewById<Button>(R.id.callButton)
		val contactsView = findViewById<TextView>(R.id.contacts)

		// load contacts from OS that are associated with WhatsApp
		val contacts = getContactsFromWhatsApp()

		// display contacts
		contactsView.text = contacts.keys.reduce { acc, s -> "$acc\n$s" }

		var inCallState = false
		button.setOnClickListener event@{
			if (inCallState) {
				inCallState = false
				button.text = "Zadzwoń"
				return@event
			}

			contacts[contact.text.toString()]?.also { contactId ->
				Intent(Intent.ACTION_VIEW)
					.setDataAndType(Uri.parse("content://com.android.contacts/data/$contactId"), WhatsAppMimeType)
					.setPackage("com.whatsapp")
					.also { startActivity(it) }

				button.text = "Zakończ"
				inCallState = true
			}
		}
	}

	private fun getContactsFromWhatsApp(): Map<String, Long> {
		var contacts : MutableMap<String, Long> = mutableMapOf()

		var cursor = applicationContext.contentResolver.query(ContactsContract.Data.CONTENT_URI,
						null, null, null,
						ContactsContract.Contacts.DISPLAY_NAME)

		while (cursor != null && cursor.moveToNext()) {
			val id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Data._ID))
			val displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME))
			val mimeType = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE))

			if (mimeType == WhatsAppMimeType)
				contacts[displayName] = id
		}

		return contacts
	}
}