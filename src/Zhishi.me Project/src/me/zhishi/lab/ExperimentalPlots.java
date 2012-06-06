package me.zhishi.lab;

import java.util.LinkedList;

public class ExperimentalPlots
{
	static LinkedList<String> list;
	
	public static void main( String[] args ) throws Exception
	{
		list = new LinkedList<String>();
		for( int i = 0; i < 3; ++i )
			list.add( String.valueOf( i ) );
		getSet( 0, 0, 0 );
	}
	
	public static void getSet( int deep, int sum, long set )
	{
		if( deep == list.size() || sum == 1000 )
		{
			if( set != 0 )
			{
				StringBuffer is = new StringBuffer();
				for( int i = 0; i < list.size(); ++i )
				{
					if( (set & (1L << i)) > 0 )
					{
						is.append( list.get( i ) + "," );
					}
				}
				System.out.println( is );
				System.out.println( deep + " " + sum );
			}
			return;
		}
		getSet( deep + 1, sum, set );
		getSet( deep + 1, sum + 1, set | (1L << deep) );
	}
}
