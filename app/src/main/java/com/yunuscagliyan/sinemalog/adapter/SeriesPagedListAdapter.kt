package com.yunuscagliyan.sinemalog.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.yunuscagliyan.sinemalog.R
import com.yunuscagliyan.sinemalog.data.api.POSTER_BASE_URL
import com.yunuscagliyan.sinemalog.data.model.NetworkState
import com.yunuscagliyan.sinemalog.data.model.Series
import com.yunuscagliyan.sinemalog.eventbus.SeriesDetailMessage
import kotlinx.android.synthetic.main.network_state_item.view.*
import org.greenrobot.eventbus.EventBus

class SeriesPagedListAdapter(
    private var context: Context,
    private var columnCount:Int
):PagedListAdapter<Series,RecyclerView.ViewHolder>(SeriesDiffCallback()) {

    val SERIES_VIEW_TYPE=1
    val NETWORK_STATE_TYPE=2
    val ADVERTISE_VIEW_TYPE=3

    private var networkState:NetworkState?=null
    var advertiseShowed=false
    var perAdbyRow=columnCount*3-2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflater= LayoutInflater.from(parent.context)
        val view:View
        if(viewType==SERIES_VIEW_TYPE){
            view=inflater.inflate(R.layout.item_popular,parent,false)
            return SeriesPagedListAdapter.SeriesItemViewHolder(view)
        }else if(viewType==NETWORK_STATE_TYPE){
            view=inflater.inflate(R.layout.network_state_item,parent,false)
            return SeriesPagedListAdapter.NetworkStateViewHolder(view)
        }else{
            view=inflater.inflate(R.layout.item_advertise,parent,false)
            return SeriesPagedListAdapter.AdItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if(getItemViewType(position)==SERIES_VIEW_TYPE){
            if(advertiseShowed && position!=0 && perAdbyRow<position){
                (holder as SeriesItemViewHolder).bindUI(getItem(position-1),context)
            }else{
                (holder as SeriesItemViewHolder).bindUI(getItem(position),context)
            }

        }else if(getItemViewType(position)==NETWORK_STATE_TYPE){
            (holder as NetworkStateViewHolder).bindUI(networkState!!)
        }else{
            (holder as AdItemViewHolder).bindUI(context)
        }
    }
    class SeriesDiffCallback: DiffUtil.ItemCallback<Series>(){
        override fun areItemsTheSame(oldItem: Series, newItem: Series): Boolean {
            return oldItem.id==newItem.id
        }

        override fun areContentsTheSame(oldItem: Series, newItem: Series): Boolean {
            return oldItem==newItem
        }

    }
    override fun getItemViewType(position: Int): Int {
        return if(hasExtraRow()&&position==itemCount-1){
            NETWORK_STATE_TYPE
        }else if(position%perAdbyRow ==0 && position!=0){
            advertiseShowed=true
            perAdbyRow=columnCount*3+1
            ADVERTISE_VIEW_TYPE
        }else{
            SERIES_VIEW_TYPE
        }
    }
    override fun getItemCount(): Int {
        return super.getItemCount()+if(hasExtraRow())1 else 0
    }
    private fun hasExtraRow():Boolean{
        return networkState!=null&&networkState!=NetworkState.LOADED
    }
    fun setNetworkState(newNetworkState: NetworkState){
        val previousState=this.networkState
        val hadExtraRow=hasExtraRow()

        this.networkState=newNetworkState
        val hasExtraRow=hasExtraRow()

        if(hadExtraRow!=hasExtraRow){
            if(hadExtraRow){
                notifyItemRemoved(super.getItemCount())
            }else{
                notifyItemInserted(super.getItemCount()-1)
            }

        }else if(hasExtraRow&&previousState!=newNetworkState){
            notifyItemChanged(super.getItemCount())
        }
    }
    class SeriesItemViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        fun bindUI(series: Series?, context: Context){
            val imageView=itemView.findViewById<ImageView>(R.id.popularIV)
            val seriesPosterURL= POSTER_BASE_URL +series!!.posterPath
            Glide.with(context)
                .load(seriesPosterURL)
                .into(imageView)
            itemView.setOnClickListener {
                if(series!=null){
                    EventBus.getDefault().post(SeriesDetailMessage(series.id))
                }
            }

        }
    }
    class NetworkStateViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        fun bindUI(networkState: NetworkState){
            if(networkState!=null&&networkState==NetworkState.LOADING){
                itemView.progressBarItem.visibility= View.VISIBLE
            }else{
                itemView.progressBarItem.visibility= View.VISIBLE
            }
            if(networkState!=null&&networkState==NetworkState.ERROR){
                itemView.error_msg.visibility= View.VISIBLE
                itemView.error_msg.text=networkState.msg
            }else{
                itemView.error_msg.visibility= View.GONE
            }
        }

    }
    class AdItemViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        fun bindUI(context: Context){
            val adView=itemView.findViewById<AdView>(R.id.rowAdItem)
            val adRequest = AdRequest.Builder()
                .build()
            adRequest.isTestDevice(context)
            adView.loadAd(adRequest)

        }

    }
}