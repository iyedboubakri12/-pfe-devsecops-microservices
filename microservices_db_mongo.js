db.createCollection("answers");

db.answers.insertMany([
    { text: "Answer 1", exam_id: 1, created_at: new Date("2023-01-01T10:00:00Z") },
    { text: "Answer 2", exam_id: 2, created_at: new Date("2023-01-02T15:00:00Z") }
]);
