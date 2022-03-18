package com.enesakkal.myinstagramclone.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.enesakkal.myinstagramclone.R
import com.enesakkal.myinstagramclone.adapter.FeedRecyclerAdapter
import com.enesakkal.myinstagramclone.databinding.ActivityFeedBinding
import com.enesakkal.myinstagramclone.model.Post
import com.google.common.io.LineReader
import com.google.common.math.LinearTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FeedActivity : AppCompatActivity() {

    private lateinit var binding : ActivityFeedBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var postArrayList :ArrayList<Post>
    private lateinit var feedAdapter : FeedRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth
        db = Firebase.firestore

        postArrayList = ArrayList<Post>()
        getData()

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        feedAdapter = FeedRecyclerAdapter(postArrayList)
        binding.recyclerView.adapter = feedAdapter
    }


    private fun getData() {
                              //Aşağıdaki value ve error bizim değişken adlarımız.Value dokümanlarımızı veriyor ,error ise hatalarımızı veriyor.
        db.collection("Posts").orderBy("date",Query.Direction.DESCENDING).addSnapshotListener { value, error -> //addsnapshot listener eklerken bir sürü
                                                                                            //gösterimi çıkıyor biz burada lambda gösterimi olanı seçeceğiz.Value,error ->
             if (error != null) {  //Yukarıda orderby ile yüklenen şeyleri tarihe göre dizebliriz.DESCENDING ıle en son atılan postu en yukarıda görebiliriz.

                 Toast.makeText(this,error.localizedMessage,Toast.LENGTH_LONG).show()
             }else {

                 if (value != null) {  //value null degıl ise bize bir değer verebiliyor.

                     if (!value.isEmpty) {//Fakat değerler boş bize gelebilir onuda kontrol etmek gerekiyor.Baştaki ! işareti

                         val documents = value.documents  //burada documents bize liste halinde bize dokümanlarımızı veriyor.Snapshot dokümanların güncel halini ifade ediyor.

                         postArrayList.clear() //Bunu yazarak 2 kez aynı postun gözükmesini engelledik.Boş bir arraylist başlatıp ardından
                         // eklemeleri aşağıda for loop içinde yapar.
                         for(document in documents) {   //

                             //Casting
                             val comment = document.get("comment") as String
                             val userEmail = document.get("userEmail") as String
                             val downloadUrl = document.get("downloadurl") as String

                             val post = Post(userEmail,comment,downloadUrl)  //ayrı bir post classa açıp post sınıfından olusturulacak objeler ıcın hangı ozellıkler gereklı olacak
                             postArrayList.add(post)        //onları belirkedik.

                         }










                     }

                     feedAdapter.notifyDataSetChanged()

                 }

             }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater                          //insta_menu menuyu yaptıgımız XML Dosyası bunu nereye bağlayacağımı secıyoruz.Çıkış yapma yeri feedactivity'de
        menuInflater.inflate(R.menu.insta_menu,menu)             //olmasıgını ıstedıgımız ıcın feedactivity'de tanımlama yaptık

        return super.onCreateOptionsMenu(menu)


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {   //örn : logout tusuna basılırsa ne olacak.2 tane item oldugu ıcın if ile kontrol etmem lazım
                                                                    //Menuyu koydugumuz seceneklerden bırısı secılırse ne olacak ona karar veriyoruz
       if (item.itemId == R.id.add_post){
           val intent = Intent(this, UploadActivity::class.java)
           startActivity(intent)

       }else if (item.itemId == R.id.Signout) {
           auth.signOut()
           val intent = Intent(this, MainActivity::class.java)
           startActivity(intent)
           finish()
       }

        return super.onOptionsItemSelected(item)
    }
}