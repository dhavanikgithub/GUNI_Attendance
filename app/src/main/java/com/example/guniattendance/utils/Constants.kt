package com.example.guniattendance.utils

object Constants {

    const val MASK = "mask"
    const val NO_MASK = "no mask"

    const val DST_WIDTH = 400
    const val DST_HEIGHT = 400
    const val ALLOWED_RADIUS = 25

//    val SELECT_ELECTIVE_CLASS = arrayListOf(1,2,3,4,5,6,8)

    val DATA: ArrayList<Triple<ArrayList<String>, ArrayList<String>, ArrayList<String>>> =
        arrayListOf(
            Triple(
                arrayListOf("abc", "xyz"),
                arrayListOf("CEITA", "CEITB", "CEITC", "CEITD"),
                arrayListOf("AB1", "AB2", "AB3", "AB4", "AB5", "AB6", "AB7", "AB8")
            ),
            Triple(
                arrayListOf("abc", "xyz"),
                arrayListOf("2CEITA", "2CEITB", "2CEITC", "2CEITD"),
                arrayListOf("2AB1", "2AB2", "2AB3", "2AB4", "2AB5", "2AB6", "2AB7", "2AB8")
            ),
            Triple(
                arrayListOf("abc", "xyz"),
                arrayListOf("3CEITA", "3CEITB", "3CEITC", "3CEITD"),
                arrayListOf("3AB1", "3AB2", "3AB3", "3AB4", "3AB5", "3AB6", "3AB7", "3AB8")
            ),
            Triple(
                arrayListOf("abc", "xyz"),
                arrayListOf("4CEITA", "4CEITB", "4CEITC", "4CEITD"),
                arrayListOf("4AB1", "4AB2", "4AB4", "4AB4", "4AB5", "4AB6", "4AB7", "4AB8")
            ),
            Triple(
                arrayListOf(
                    "Computer Architecture & Organization",
                    "Software Engineering",
                    "Computer Networks",
                    "Aptitude Skill Building-I",
//                    "Computer Graphics & Visualization",
//                    "Software Packages",
//                    "Mobile Application Development",
//                    "Innovation & Entrepreneurship",
//                    "Machine Learning",
//                    "Computational Data Analytics"
                ),
                arrayListOf("CEIT-A", "CEIT-B", "CEIT-C", "CEIT-D"),
                arrayListOf(
                    "AB1",
                    "AB2",
                    "AB3",
                    "AB4",
                    "AB5",
                    "AB6",
                    "AB7",
                    "AB8",
                    "AB9",
                    "AB10",
                    "AB11",
                    "AB12",
                    "AB13",
                    "AB14",
                    "AB15",
                    "AB16",
                    "AI1",
                    "AI2"
                )
            ),
            Triple(
                arrayListOf("abc", "xyz"),
                arrayListOf("6CEITA", "6CEITB", "6CEITC", "6CEITD"),
                arrayListOf("6AB1", "6AB2", "6AB6", "6AB6", "6AB6", "6AB6", "6AB7", "6AB8")
            ),
            Triple(
                arrayListOf(
                    "Big Data Analytics",
                    "Compiler Design",
                    "Forensics & Cyber Law",
                    "ML Ops"
                ),
                arrayListOf("CEIT-A", "CEIT-B", "CEIT-C"),
                arrayListOf(
                    "AB1",
                    "AB2",
                    "AB3",
                    "AB4",
                    "AB5",
                    "AB6",
                    "AB7",
                    "AB8",
                    "AB9",
                    "AB10",
                    "AB11",
                    "AB12",
                    "AI"
                )
            ),
            Triple(
                arrayListOf("abc", "xyz"),
                arrayListOf("8CEITA", "8CEITB", "8CEITC", "8CEITD"),
                arrayListOf("8AB1", "8AB2", "8AB8", "8AB8", "8AB8", "8AB8", "8AB8", "8AB8")
            )
        )
}