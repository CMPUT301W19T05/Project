package ca.ualberta.cmput301w19t05.sharebook.tools;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ca.ualberta.cmput301w19t05.sharebook.R;
import ca.ualberta.cmput301w19t05.sharebook.models.Record;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Record> mRecord;
    private LayoutInflater mInflater;
    private MyRecyclerViewAdapter.ItemClickListener mClickListener;
    private Context mContext;
    private FirebaseHandler firebaseHandler;

    public NotificationAdapter(List<Record> mRecord, Context mContext) {
        this.mRecord = mRecord;
        this.mContext = mContext;
        this.mInflater = LayoutInflater.from(mContext);
        firebaseHandler = new FirebaseHandler(mContext);
    }


    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = mInflater.inflate(R.layout.item_notification, viewGroup, false);
        return new NotificationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.ViewHolder viewHolder, int i) {

        viewHolder.requesterName.setText(mRecord.get(i).getBorrowerName());
        viewHolder.notificationType.setText(mRecord.get(i).getStatus());
        viewHolder.bookName.setText(mRecord.get(i).getBookName());
    }

    @Override
    public int getItemCount() {
        return mRecord.size();
    }
    public void setClickListener(MyRecyclerViewAdapter.ItemClickListener itemClickListener) {

        this.mClickListener = itemClickListener;
    }

    public void addRecord(Record record){
        mRecord.add(record);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView requesterName;
        public TextView notificationType;
        public TextView bookName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            requesterName = itemView.findViewById(R.id.user_name);
            notificationType = itemView.findViewById(R.id.notification_type);
            bookName = itemView.findViewById(R.id.book_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mClickListener != null) mClickListener.onItemClick(v, getAdapterPosition());

        }
    }
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
