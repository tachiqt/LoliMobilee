package com.example.lolimobilee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuestionsProvider {

    public static List<Topic> getTopics() {
        List<Topic> topics = new ArrayList<>();

        topics.add(new Topic(
                "Conversational Skills",
                Arrays.asList(
                        "I can confidently introduce myself to others, even if I feel nervous.",
                        "I am able to pick up on subtle cues about topics that others may want to discuss.",
                        "I can identify shared interests with others to create a natural connection.",
                        "I make an effort to bring up previous conversations to show I remember details about others.",
                        "I can recognize when it’s appropriate to add humor and when it’s best to stay serious.",
                        "I am comfortable discussing both lighthearted topics and more serious, personal issues.",
                        "I rarely feel “lost for words” in social interactions, even if the topic is unfamiliar.",
                        "I make an effort to consider the other person’s interests when choosing conversation topics.",
                        "I can adjust my language and tone based on whether I’m speaking with friends, family, or colleagues.",
                        "I feel comfortable sharing my opinions, even if they may differ from those of the people I’m with."
                )
        ));

        topics.add(new Topic(
                "Listening and Empathy",
                Arrays.asList(
                        "I am patient when others are explaining something, even if I already understand it.",
                        "I avoid giving unsolicited advice and try to listen fully before offering help.",
                        "I can tell when someone needs space or time to process their emotions.",
                        "I make an effort to validate others’ feelings, even if I don’t fully agree with them.",
                        "I don’t rush people when they are sharing their feelings or thoughts.",
                        "I can distinguish between when someone wants advice and when they simply need to be heard.",
                        "I avoid trying to “one-up” others’ experiences by sharing my own similar situations.",
                        "I’m careful not to interrupt, even if I have an important thought to share.",
                        "I make an effort to observe non-verbal cues, like tone and pace, to better understand others.",
                        "I try to express understanding verbally by saying things like, “I can see why you feel that way."
                )
        ));

        topics.add(new Topic(
                "Body Language",
                Arrays.asList(
                        "I consciously avoid crossing my arms or showing closed-off body language during conversations.",
                        "I am aware of my own body posture and how it might be perceived by others."
                )
        ));

        topics.add(new Topic(
                "Conflict Resolution and Patience",
                Arrays.asList(
                        "I handle disagreements with others calmly and constructively.",
                        "I can admit my mistakes and learn from them in conflict situations."
                )
        ));

        return topics;
    }
}
