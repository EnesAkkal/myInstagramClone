package com.enesakkal.myinstagramclone.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.enesakkal.myinstagramclone.databinding.ActivityUploadBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.*

class UploadActivity : AppCompatActivity() {

    private lateinit var binding : ActivityUploadBinding
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedPicture : Uri? = null
    private lateinit var storage : FirebaseStorage
    private lateinit var firestore : FirebaseFirestore
    private lateinit var auth: FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        registerLauncher()

        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage

    }



    fun upload (view : View) {   //Upload işlemini yapmak için firebase kullanmamız gerekli

        //Universal unique ID
        val uuid = UUID.randomUUID()  //uuid kullanmamızın sebebi aynı isimli resimler kaydedildiği zaman override edip eskisini silip yenisini göstermesi
        val imageName = "$uuid.jpg"     //örn: 2 tane images isimli resim kaydedilirse ikisi de gösterilmez sadece en yeni olan eski olanın üstüne override olur.

        val reference = storage.reference //Referans nerde oldugumuzu takip etmeye yarayan bir objedir ve referansı storage'dan turetebiliyoruz.Solda yazan kod bize boş bir storage verir.
        val imageReference = reference.child("images").child(imageName) //images klasörünü aç ve içine imageName objesinden oluşturulan bir resim koy

        if (selectedPicture != null) {   //kullanıcı seçmeden resim upload yapmazsın diye if kontrolü yapıyoruz.

            //kullanıcı upload ederse firestore kaydediyoruz onSucess altında
            imageReference.putFile(selectedPicture!!).addOnSuccessListener {
                //Download Url -->FireStore
                val uploadPictureReference = storage.reference.child("images").child(imageName)
                uploadPictureReference.downloadUrl.addOnSuccessListener {
                     val downloadUrl = it.toString()                         //uri bize nereye kayıtlı oldugunu verıyor resımlerın


                    if (auth.currentUser != null) {

                        val postMap = hashMapOf<String, Any>()  //Verileri kaydederken hashmap kullanacağız.Kaydedeceğimiz verilerin keywordu string olurken value kısmında
                        postMap.put("downloadurl",downloadUrl)  //string,Integer, double vs olabileceğinden any dedik.Mesela tarih bir Int oldugu ıcın sadece Strıng yazamayız.
                        postMap.put("userEmail",auth.currentUser!!.email!!)
                        postMap.put("comment",binding.commentText.text.toString())
                        postMap.put("date",Timestamp.now())   // timestamp, postun güncel oldugu zamanı alıp firebase kaydediyor

                        firestore.collection("Posts").add(postMap).addOnSuccessListener {
                        //Yukarıda Posts isimli bir koleksiyon oluşturduk ve eklemek istediğimiz veriyi belirledik "postMap"
                            finish()

                        }.addOnFailureListener {

                            Toast.makeText(this@UploadActivity,it.localizedMessage,Toast.LENGTH_LONG).show()

                        }
                    }



                }


            }.addOnFailureListener{
                Toast.makeText(this,it.localizedMessage,Toast.LENGTH_LONG).show()
            }

        }


    }

    fun selectImage (view : View) {


        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",) {
                        //Request Permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }.show()

            } else {
            //Request Permission
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

            }

        } else {
            val intentToGallery =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
        }

    }

    private fun registerLauncher () {

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == RESULT_OK) {

                val intentFromResult = result.data
                if (intentFromResult != null) {
                    selectedPicture = intentFromResult.data
                    selectedPicture?.let {
                        binding.imageView.setImageURI(it)
                    }
                }

            }

        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                //Permission Granted
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else  {
                //Permission Denied
                Toast.makeText(this,"Permission is needed for upload",Toast.LENGTH_LONG).show()

            }
        }




    }

}

