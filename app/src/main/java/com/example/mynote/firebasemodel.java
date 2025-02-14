package com.example.mynote;

import com.google.firebase.Timestamp;


 public class firebasemodel {

        private String title;
        private String content;
        private Timestamp editTime;

        public firebasemodel() { }

        public firebasemodel(String title, String content, Timestamp editTime) {
            this.title = title;
            this.content = content;
            this.editTime = editTime;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Timestamp getEditTime() {
            return editTime;
        }

        public void setEditTime(Timestamp editTime) {
            this.editTime = editTime;
        }
    }


