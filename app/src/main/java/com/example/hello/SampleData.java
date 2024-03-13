package com.example.hello;

public class SampleData {

    private String user_id;
    private String receipt_review_content;
    private String receipt_date;

    public SampleData(String user_id,String receipt_review_content, String receipt_date){
        this.user_id = user_id;
        this.receipt_review_content = receipt_review_content;
        this.receipt_date = receipt_date;
    }

    public String getUser_id()
    {
        return this.user_id;
    }

    public String getReceipt_review_content()
    {
        return this.receipt_review_content;
    }

    public String getReceipt_date()
    {
        return this.receipt_date;
    }


}
