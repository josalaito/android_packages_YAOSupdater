package org.arasthel.yaos.ui;

import org.arasthel.yaos.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Cling extends Activity{
	
	private TextView clingTitle;
	private TextView clingText;
	private Button clingContinue;
	private ImageView clingBack;
	private int density;
	private int yaosIconY;
	public static short scenary = 0;
	public static double[] dimens;
	public static boolean isCircle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cling);
		clingTitle = (TextView) findViewById(R.id.clingTitleText);
		clingText = (TextView) findViewById(R.id.clingText);
		clingContinue = (Button) findViewById(R.id.clingContinue);
		density = (int) getResources().getDisplayMetrics().densityDpi;
		dimens = new double[4];
		isCircle = false;
		yaosIconY = (int) findViewById(R.id.yaosIconCling).getY();
		clingBack = (ImageView) findViewById(R.id.clingBack);
		scenary = 0;
		updateScenary();
	}
	
	private void updateScenary(){
		View aux;
		Log.d("DENSITY","Density value: "+density);
		switch(scenary){
		case 0:
			clingTitle.setText("Bienvenido a YAOS Updater");
			clingText.setText("Este tutorial te enseñará cómo funciona el programa. Si no quieres ver el tutorial, pulsa el botón Atrás o haz click en la imagen de YAOS en la esquina inferior derecha.");
			break;
		case 1:
			setDimens(0, 0, 4.7*density, 0.375*density);
			clingTitle.setY((float) (0.45*density));
			clingTitle.setText("Pestañas:");
			clingText.setText("YAOS cuenta con 4 pestañas: Actualizaciones, Parches, Google Apps e Instalación.\n\n-En Actualizaciones encontrarás las ROMs para descargar.\n" +
					"-En Parches encontrarás parches aplicacbles sobre la ROM, que le darán más opciones o corregirán errores.\n" +
					"-En Google Apps encontrarás los pack con las aplicaciones de Google para usarlas en la ROM.");
			break;
		case 2:
			setDimens(6*density, 0, 6.875*density, 0.375*density);
			clingTitle.setText("Actualizar:");
			clingText.setText("Para obtener el listado de archivos que el actualizador puede descargar, debe hacer click en el botón actualizar.");
			break;
		case 3:
			aux = ((Activity) Principal.getContext()).findViewById(R.id.update_list_fragment);
			setDimens(0, aux.getY()+0.3125*density, aux.getWidth()+1, 4.7*density);
			clingTitle.setX((float) (dimens[2] + 0.5*density));
			clingTitle.setText("Listado de archivos");
			clingText.setText("La lista de archivos disponibles se podrá ver a la izquierda. Debe seleccionar de la lista aquel archivo que quiera descargar.");
			break;
		case 4:
			aux = ((Activity) Principal.getContext()).findViewById(R.id.update_list_fragment);
			setDimens(aux.getWidth(), 0.44*density, 8*density, 3.125*density);
			clingTitle.setX((float) (0.25*density));
			clingTitle.setY((float) (3.125*density));
			clingTitle.setText("Información de la actualización");
			clingText.setText("Al seleccionar un archivo, se le mostrará su información:\n\n-Nombre descriptivo.\n-Versión del archivo o versión indicada para instalar.\n-Nombre real del archivo.\n-Descripción de los contenidos del archivo o de su utilidad.");
			break;
		case 5:
			setDimens(6.875*density, 0, 7.625*density, 0.375*density);
			clingTitle.setY((float) (0.3125*density));
			clingTitle.setText("Descargar archivo");
			clingText.setText("Una vez comprobado que se quiere descargar el archivo, debe de pulsarse el botón de Descargar.");
			break;
		case 6:
			clingBack.setImageDrawable(getResources().getDrawable(R.drawable.yaos2));
			setDimens(5.2*density, 3.4*density, 8*density, 3.875*density);
			aux = findViewById(R.id.yaosIconCling);
			aux.setY((float) (0.5*density));
			clingTitle.setText("El archivo comenzará a descargarse");
			clingText.setText("Se mostrará una notificación con la información de la descarga.");
			break;
		case 7:
			clingBack.setImageDrawable(getResources().getDrawable(R.drawable.yaos1));
			setDimens(4.25*density, 4*density, 6*density, 4.4*density);
			clingTitle.setText("Cuando termine la descarga");
			clingText.setText("Debes pulsar este botón para que el archivo descargado se instale más adelante.");
			break;
		case 8:
			clingBack.setImageDrawable(getResources().getDrawable(R.drawable.yaos3));
			setDimens(2.5*density,0.375*density,7.875*density,3.125*density);
			aux = findViewById(R.id.yaosIconCling);
			aux.setY((float) (3.4*density));
			clingTitle.setY((float) (3.3*density));
			clingTitle.setText("Instalación");
			clingText.setText("Este es el menú de instalación. En él hay 4 entradas, una para cada categoría de archivo y una extra, para poder instalar hasta 4 archivos. Además, cuenta con las opciones de hacer una copia de seguridad del teléfono desde el modo de recuperación y eliminar todos los datos del sistema y aplicación del teléfono (hacer wipe). Para quitar un archivo de una de la entradas basta con pulsar en la cruz que hay debajo de esa entrada.");
			break;
		case 9:
			setDimens(0,0,2.3125*density,5*density);
			clingTitle.setX((float) (2.5*density));
			clingTitle.setY((float) (0.3125*density));
			clingTitle.setText("Listado de archivos de instalación");
			clingText.setText("A la izquierda hay un listado de archivos descargados que pueden instalarse. Para añadirlos a la instalación basta con pulsar en uno y elegir la entrada a la que añadirlo. También hay un botón de Eliminar para borrar archivos que ya no sean necesarios.");
			break;
		case 10:
			setDimens(6.875*density, 0, 7.625*density, 0.375*density);
			clingTitle.setX((float) (0.25*density));
			clingTitle.setText("Instalar archivos");
			clingText.setText("Una vez seleccionados los archivos a instalar, pulse el botón Aplicar. Se hará una comprobación de errores de los archivos y en caso de no haberlos, se procederá a reinciar en modo de recuperación para instalarlos.");
			break;
		case 11:
			setDimens(7.625*density, 0, 8*density, 0.375*density);
			clingTitle.setText("Opciones");
			clingText.setText("El programa además contiene un menú de Ajustes donde cambiar opciones como la carpeta de descarga, los archivos a mostrar, etc.");
			break;
		case 12:
			setDimens(0,0,0,0);
			clingTitle.setText("Fin del tutorial");
			clingText.setText("Parece que ya estás listo para empezar a usar YAOS. Espero que te sea muy útil :)");
			break;
		case 13:
			clingContinue.setText("Terminar");
			finish();
		}
		align();
	}
	
	public void exit(View v){
		this.finish();
	}
	
	public void next(View v){
		scenary++;
		updateScenary();
		this.findViewById(R.id.surfaceView).invalidate();
	}
	
	private void align(){
		clingText.setX(clingTitle.getX());
		clingContinue.setX(clingText.getX());
		clingText.setY(clingTitle.getY()+clingTitle.getHeight()+10);
		clingContinue.setY(clingText.getY()+clingText.getHeight()+10);
	}
	
	private void setDimens(double a, double b, double c, double d){
		dimens[0] = a;
		dimens[1] = b;
		dimens[2] = c;
		dimens[3] = d;
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(Principal.getContext());
		Editor edit = sp.edit();
		edit.putBoolean("firstLaunch", false);
		edit.commit();
	}
	
}
