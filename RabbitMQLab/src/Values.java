import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class Values {
    public static final String DOCTORS_HOSPITAL_QUEUE_NAME = "a";
    public static final String EXCHANGE_DOCTORS_TECHNICIAN = "b";
    public static final String ADMIN_LOG_QUEUE_NAME = "h";
    public static final String ADMIN_INFO_EXCHANGE = "dsdasdfdfddfsfsdfsfhd";

    private static final String[] bodyParts = {"knee", "hip", "elbow"};

    private static final String[] firstNames = {
            "Orson", "Mohammad", "Rachel", "Ivy", "Meghan", "Jarrod", "Sophia", "Amos", "Dai", "Nathan",
            "Zelda", "Mercedes", "Damon", "Georgia", "Aimee", "Cole", "Harper", "Ariana", "Justine", "Emily",
            "Branden", "Ethan", "Keiko", "Haley", "Nichole", "Rahim", "Jeremy", "Marsden", "Blossom", "Quail",
            "Brady", "Jakeem", "Aubrey", "Athena", "Bree", "Jasmine", "Jaime", "Evan", "April", "Faith", "Ivana",
            "Gisela", "Eden", "Jin", "Hadassah", "Josiah", "Driscoll", "Jasper", "Ria", "Carol", "Magee", "Yoshio",
            "Harriet", "Melissa", "Keiko", "Xerxes", "Griffith", "Finn", "Buffy", "Uriel", "Valentine", "Nora", "Keegan",
            "Simon", "Bevis", "Lawrence", "Lois", "Jared", "Ivory", "Gavin", "TaShya", "Hammett", "Nelle", "Abraham",
            "Lev", "Eleanor", "Meghan", "Hakeem", "Cailin", "Arthur", "Reese", "Uriah", "Sopoline", "Logan", "Ryan", "Serina",
            "Ignacia", "Amy", "Lawrence", "Lynn", "Raphael", "Halla", "Shea", "Savannah", "Latifah", "Joy", "Lev", "Yetta", "Donovan", "Jolie"
    };

    private static final String[] lastNames = {
            "Dickerson","Hartman","Pugh","Hampton","Hooper","Bender","Craft",
            "Randolph","Sosa","Mcbride","Duran","Baldwin","Branch","Cochran","Snyder",
            "Atkins","Love","Flynn","Brennan","Blackburn","Cabrera","Lester","Rice","Johnson",
            "Forbes","Gonzales","Olsen","Ross","Carrillo","Williams","Chang","Delacruz","Dominguez",
            "Gay","Curtis","Wilder","Stephens","Hampton","Bradley","Weaver","Craig","Perry","Campos",
            "Bass","Booker","Parker","Salinas","Gallegos","Hodges","Martinez","Duran","Bradford","Lucas",
            "Lynch","Green","Hanson","Hendrix","Riddle","Avila","Jimenez","Santiago","Holder","Snow","Ray",
            "Sharpe","Stewart","Chandler","Fry","Greene","Tyson","Foley","Estes","Clemons",
            "Pierce","Dixon","Leon","Guerrero","Sears","Workman","Huffman","Clarke","Hewitt",
            "Nielsen","England","Petersen","Vaughan","Brown","Clarke","Beach","Gilliam","Carrillo",
            "Mullins","Nguyen","Glass","Humphrey","England","Reyes","Glass","Pacheco","Gallagher"
    };

    public static String[] generateRandomParts(){
        Collections.shuffle(Arrays.asList(bodyParts));
        return new String[]{bodyParts[0], bodyParts[1]};
    }
    public static String generateAche(){
        return bodyParts[new Random().nextInt(3)];
    }
    public static String generatePerson(){
        int firstNameIndex = new Random().nextInt(firstNames.length);
        int lastNameIndex = new Random().nextInt(lastNames.length);
        return firstNames[firstNameIndex] + " " + lastNames[lastNameIndex];
    }
}
