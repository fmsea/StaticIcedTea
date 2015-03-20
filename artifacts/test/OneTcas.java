package test;

public class OneTcas {
//	public static int OLEV=600;
//	public static int MAXALTDIFF=600;
//	public static int MINSEP=300;
//	public static int NOZCROSS=100;
//	
//
//	public static int Cur_Vertical_Sep;
//	public static boolean High_Confidence;
//	public static boolean Two_of_Three_Reports_Valid;
//
//	public static int Own_Tracked_Alt;
//	public static int Own_Tracked_Alt_Rate;
//	public  static int Other_Tracked_Alt;
//
//	public static int Alt_Layer_Value;		/* 0, 1, 2, 3 */
//	//public static int[] Positive_RA_Alt_Thresh;
//	static int Positive_RA_Alt_Thresh_0;
//	static int Positive_RA_Alt_Thresh_1;
//	static int Positive_RA_Alt_Thresh_2;
//	static int Positive_RA_Alt_Thresh_3;
//
//	public static int Up_Separation;
//	public  static int Down_Separation;
//	
//
//	/* state variables */
//	public  static int Other_RAC;			/* NO_INTENT, DO_NOT_CLIMB, DO_NOT_DESCEND */
//	public static int NO_INTENT=0;
//	public static int DO_NOT_CLIMB= 1;
//	public static int DO_NOT_DESCEND=2;
//	
//	public static int Other_Capability;		/* TCAS_TA, OTHER */
//	public static int TCAS_TA=1;
//	public static int OTHER=2;
//	
//	public static int Climb_Inhibit;		/* true/false */
//	
//	public static int UNRESOLVED=0;
//	public static int UPWARD_RA=1;
//	public static int DOWNWARD_RA=2;
	public static void main(String[] argv){
		int a1=0,a2=0,a3=0,a4=0,a5=0,a6=0,a7=0,a8=0,a9=0,a10=0,a11=0,a12=0;
		int argc=argv.length+1;
		if(argc<13){
			System.out.println("Error: Command line arguments are");
			System.out.println("Cur_Vertical_Sep, High_Confidence, Two_of_Three_Reports_Valid");
			System.out.println("Own_Tracked_Alt, Own_Tracked_Alt_Rate, Other_Tracked_Alt");
			System.out.println("Alt_Layer_Value, Up_Separation, Down_Separation");
			System.out.println("Other_RAC, Other_Capability, Climb_Inhibit");
			return;
		}
		else{
		a1=Integer.parseInt(argv[0]+"");
		a2=Integer.parseInt(argv[1]+"");
		a3=Integer.parseInt(argv[2]+"");
		a4=Integer.parseInt(argv[3]+"");
		a5=Integer.parseInt(argv[4]+"");
		a6=Integer.parseInt(argv[5]+"");
		a7=Integer.parseInt(argv[6]+"");
		a8=Integer.parseInt(argv[7]+"");
		a9=Integer.parseInt(argv[8]+"");
		a10=Integer.parseInt(argv[9]+"");
		a11=Integer.parseInt(argv[10]+"");
		a12=Integer.parseInt(argv[11]+"");
		int alt_sep = main2(a1,a2,a3,a4,a5,a6,a7,a8,a9,a10,a11,a12);
		}
	}
	
	static int main2(int a1,int a2,int a3,int a4,int a5,int a6,int a7, int a8,
			int a9, int a10, int a11, int a12){
		 int OLEV=600;
		 int MAXALTDIFF=600;
		 int MINSEP=300;
		 int NOZCROSS=100;
		

		 int Cur_Vertical_Sep;
		 boolean High_Confidence;
		 boolean Two_of_Three_Reports_Valid;

		 int Own_Tracked_Alt;
		 int Own_Tracked_Alt_Rate;
		int Other_Tracked_Alt;

		 int Alt_Layer_Value;		/* 0, 1, 2, 3 */
		// int[] Positive_RA_Alt_Thresh;

		 int Up_Separation;
		int Down_Separation;
		

		/* state variables */
		int Other_RAC;			/* NO_INTENT, DO_NOT_CLIMB, DO_NOT_DESCEND */
		 int NO_INTENT=0;

		
		 int Other_Capability;		/* TCAS_TA, OTHER */
		 int TCAS_TA=1;

		
		 int Climb_Inhibit;		/* true/false */
		
		 //int UNRESOLVED=0;
		 int UNRESOLVED=5;// to aid SA
		 int UPWARD_RA=1;
		 int DOWNWARD_RA=2;
		//System.out.println("begin");
		    int Positive_RA_Alt_Thresh_0= 400;
		    int Positive_RA_Alt_Thresh_1= 500;
		    int Positive_RA_Alt_Thresh_2= 640;
		    int Positive_RA_Alt_Thresh_3= 740;
		Cur_Vertical_Sep=a1;
		if(a2==0){
	    	High_Confidence=false;
	    }
	    else{
	    	High_Confidence=true;
	    }
		if(a3==0){
	    	Two_of_Three_Reports_Valid=false;
	    }
	    else{
	    	Two_of_Three_Reports_Valid=true;
	    }
	    //Two_of_Three_Reports_Valid = Integer.parseInt(argv[3]+"");
	    Own_Tracked_Alt = a4;
	    Own_Tracked_Alt_Rate = a5;
	    Other_Tracked_Alt = a6;
	    Alt_Layer_Value = a7;
	    Up_Separation =a8;
	    Down_Separation = a9;
	    Other_RAC = a10;
	    Other_Capability = a11;
	    Climb_Inhibit = a12;
	    boolean enabled, tcas_equipped, intent_not_known;
	    boolean need_upward_RA, need_downward_RA;
	    int alt_sep;

	    enabled = High_Confidence && (Own_Tracked_Alt_Rate <= OLEV) && (Cur_Vertical_Sep > MAXALTDIFF);
	    tcas_equipped = Other_Capability == TCAS_TA;
	    intent_not_known = Two_of_Three_Reports_Valid && Other_RAC == NO_INTENT;
	    
	    //alt_sep = UNRESOLVED;
	   alt_sep = 0; // new initialization
	    
	    int ALIM;
	    if(Alt_Layer_Value==0) 
	    	ALIM= Positive_RA_Alt_Thresh_0;
		else if(Alt_Layer_Value==1) 
			ALIM= Positive_RA_Alt_Thresh_1;
		else if(Alt_Layer_Value==2) 
			ALIM= Positive_RA_Alt_Thresh_2;
		else 
			ALIM= Positive_RA_Alt_Thresh_3;
	    
		 boolean Own_Below_Threat= Own_Tracked_Alt < Other_Tracked_Alt;
			

	     boolean Own_Above_Threat= Other_Tracked_Alt < Own_Tracked_Alt;
			
	    
	    if (enabled && ((tcas_equipped && intent_not_known) || !tcas_equipped))
	    {
		    int upward_preferred;
		    //int upward_crossing_situation;
		    int temp;
		    boolean result;
		    if(Climb_Inhibit >0)
				temp= Up_Separation + NOZCROSS;
			else 
				temp=Up_Separation;
		    
		    
		    if(temp > Down_Separation){
		    	upward_preferred=1;
		    }
		    else{
		    	upward_preferred=0;
		    }
		    if (upward_preferred!=0)
		    {
		    	
			result = !(Own_Below_Threat) || ((Own_Below_Threat) && (!(Down_Separation > ALIM))); 
		    }
		    else
		    {	
			result = Own_Above_Threat && (Cur_Vertical_Sep >= MINSEP) && (Up_Separation >= ALIM);
		    }
		    boolean Non_Crossing_Biased_Climb=result;
	    	
		    

		    if(Climb_Inhibit >0)
				temp= Up_Separation + NOZCROSS;
			else 
				temp= Up_Separation;
		    
		    if(temp > Down_Separation){
		    	upward_preferred=1;
		    }
		    else{
		    	upward_preferred=0;
		    }
		    //upward_preferred = Inhibit_Biased_Climb() > Down_Separation;
		    if (upward_preferred!=0)
		    {
			result = Own_Below_Threat && (Cur_Vertical_Sep >= MINSEP) && (Down_Separation >= ALIM);
		    }
		    else
		    {
			result = !(Own_Above_Threat) || ((Own_Above_Threat) && (Up_Separation >= ALIM));
		    }
		    boolean  Non_Crossing_Biased_Descend=result;
	
			need_upward_RA = Non_Crossing_Biased_Climb && Own_Below_Threat;
			need_downward_RA = Non_Crossing_Biased_Descend && Own_Above_Threat;
			if (need_upward_RA && need_downward_RA)
		        /* unreachable: requires Own_Below_Threat and Own_Above_Threat
		           to both be true - that requires Own_Tracked_Alt < Other_Tracked_Alt
		           and Other_Tracked_Alt < Own_Tracked_Alt, which isn't possible */
			    alt_sep = UNRESOLVED;
			else if (need_upward_RA)
			    alt_sep = UPWARD_RA;
			else if (need_downward_RA)
			    alt_sep = DOWNWARD_RA;
			else
			    alt_sep = UNRESOLVED;	// make sure that alt_sep has been altered
			
			//Make sure that alt_sep has been altered
		    if(alt_sep <= 0){ // should always be resolved if the previous branch is taken
		    	System.out.println("assert"); // this condition should never occur!
		    	System.exit(2);
		    }
		    
		    }
	    	//System.out.println("alt_sep:"+alt_sep);
	    return alt_sep;
	}
}


