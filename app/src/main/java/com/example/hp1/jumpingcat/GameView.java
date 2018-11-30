package com.example.hp1.jumpingcat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.os.Handler;
import android.widget.Toast;


public class GameView extends View {


    Handler handler;//para hacer acciones es cadena con delay
    Runnable runnable;
    int UPDATE_MILLIS=30; //Velocidad del juego con la q se actualiza, no menos de 30 plox xD
    Bitmap background;
    Display display;
    Point point;
    int dWith, dHeight;//dispositivo con ancho y altura
    Rect rect;
    private Paint paint;

    //Bitmap para el gato saltarin
    Bitmap[] cat;
    int posicion=0;//posicion del gato en la q se encuentra (visual)
    int velocidadY=0,gravedad=4; //grav base era 3
    int velocidadX = 30;//velocidad de mov X
    int catx,caty;//posicion del gato
    int dirx = -1;//+1 der -1 izq

    //puntuacion
    int puntuacion = 0;

    //Evento perder
    boolean isGameOver = false;

    //Wall
    Bitmap wall1,wall2,wall3;//tres tamaños de wall
    Bitmap[] walls; //las walls del a al e rotan entre si xd
    int[][] wallxy; //posiciones que varian de las walls

    //Piso
    Bitmap piso;
    int pisox,pisoy;
    int muerte;//limite y de caida para perder, inicia con el valor del piso luego cambia al final de pantalla

    //nube piso y cielo
    Bitmap nubepiso,nubecielo;
    int nubep, nubec,nubex;

    //camera limite que hace q se mueva lo demas no el gato
    int limxizq,limxder,limy;
    int velocidadlim; // valor con el q entra a la zona límite

    //sonidos
    public static MediaPlayer ost;
    MediaPlayer catjump,catdeath;
    int record;

    //blockeo de paredes
    Boolean direccion;
    int dirbloc;//direccion de bloqueo

    //salto
    int cantsaltos;
    int hmax;

    //inicio
    int inicio;


    public GameView(Context context) {

        super(context);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                invalidate();//llama a onDraw()
            }
        };
        background = BitmapFactory.decodeResource(getResources(),R.drawable.colorbackground);
        display= ((Activity)getContext()).getWindowManager().getDefaultDisplay();
        point = new Point();
        display.getSize(point);
        dWith = point.x;
        dHeight = point.y;
        hmax=dHeight; //determina la altura maxima para la puntuación
        rect = new Rect(0,0,dWith,dHeight);
        cat = new Bitmap[6];
        Bitmap cat0 = BitmapFactory.decodeResource(getResources(),R.drawable.gato_1_izq); //posicion gato normal izq
        cat[0] = Bitmap.createScaledBitmap(cat0,200,200,false);
        Bitmap cat1 = BitmapFactory.decodeResource(getResources(),R.drawable.gato_1_der); //posicion gato normal der
        cat[1] = Bitmap.createScaledBitmap(cat1,200,200,false);
        Bitmap cat2 = BitmapFactory.decodeResource(getResources(),R.drawable.gato_2_izq); //posicion gato saltando izq
        cat[2] = Bitmap.createScaledBitmap(cat2,210,200,false);
        Bitmap cat3 = BitmapFactory.decodeResource(getResources(),R.drawable.gato_2_der); //posicion gato saltando der
        cat[3] = Bitmap.createScaledBitmap(cat3,210,200,false);
        Bitmap cat4 = BitmapFactory.decodeResource(getResources(),R.drawable.gato_3_izq); //posicion gato morido izq
        cat[4] = Bitmap.createScaledBitmap(cat4,220,200,false);
        Bitmap cat5 = BitmapFactory.decodeResource(getResources(),R.drawable.gato_3_der); //posicion gato morido der
        cat[5] = Bitmap.createScaledBitmap(cat5,220,200,false);
        //posicion inicial del gato se basa en la imagen "gato normal" el punto mas arriba izquierda de la imagen gato
        catx = dWith*3/4 - cat[0].getWidth()/2;
        caty = dHeight*5/7 - cat[0].getHeight()/2;

        //wall dibujo
        Bitmap wallito1 = BitmapFactory.decodeResource(getResources(),R.drawable.v1); //posicion gato normal izq
        wall1=Bitmap.createScaledBitmap(wallito1,48,309,false);
        Bitmap wallito2 = BitmapFactory.decodeResource(getResources(),R.drawable.v2); //posicion gato normal izq
        wall2=Bitmap.createScaledBitmap(wallito2,48,489,false);
        Bitmap wallito3 = BitmapFactory.decodeResource(getResources(),R.drawable.v3); //posicion gato normal izq
        wall3=Bitmap.createScaledBitmap(wallito3,48,804,false);

        //piso dibujo
        piso = BitmapFactory.decodeResource(getResources(),R.drawable.piso);
        pisox = (dWith/2)-(piso.getWidth()/2);
        pisoy = dHeight-piso.getHeight();

        //nube dibujo
        nubepiso=BitmapFactory.decodeResource(getResources(),R.drawable.nube1);
        nubecielo=BitmapFactory.decodeResource(getResources(),R.drawable.nuber2);
        nubep=dHeight/2-nubepiso.getHeight()/2;
        nubec=nubep-nubecielo.getHeight();
        nubex=dWith/2;


        //limites de efecto camara
        limy=540;limxizq=200;limxder=900;
        //limite inicial muerte del piso
        muerte=dHeight-piso.getHeight();

        //walls dibujo randomeado
        walls = new Bitmap[5];
        wallxy = new int[5][2];
        walls[0]= wall3;wallxy[0][0]=dWith/2-380;wallxy[0][1]=muerte-50-wall3.getHeight();//x=posicion x del anterior + randomx *** y=posicion y anterior - tamaño este - randomy
        walls[1]= wallrandom();wallxy[1][0]=wallxy[0][0]+randomx();wallxy[1][1]=wallxy[0][1]-walls[1].getHeight()-randomy();
        walls[2]= wallrandom();wallxy[2][0]=wallxy[1][0]+randomx();wallxy[2][1]=wallxy[1][1]-walls[2].getHeight()-randomy();
        walls[3]= wallrandom();wallxy[3][0]=wallxy[2][0]+randomx();wallxy[3][1]=wallxy[2][1]-walls[3].getHeight()-randomy();
        walls[4]= wallrandom();wallxy[4][0]=wallxy[3][0]+randomx();wallxy[4][1]=wallxy[3][1]-walls[4].getHeight()-randomy();

        //musica
        ost=MediaPlayer.create(getContext(),R.raw.playwhite);
        ost.start();
        catjump=MediaPlayer.create(getContext(),R.raw.cutecat);
        catdeath=MediaPlayer.create(getContext(),R.raw.deathcat);
        record=0;

        //saltos
        cantsaltos=0;

        //inicio
        inicio=0;

        //webadas borrables

    }



//**************************************************************************************************************************
    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);//dibujar en view dentro del onDraw
        canvas.drawBitmap(background,null,rect,null);

        //dibuja nubes
        canvas.drawBitmap(nubepiso,nubex,nubep,null);
        canvas.drawBitmap(nubepiso,nubex-nubepiso.getWidth(),nubep,null);
        //xder
        canvas.drawBitmap(nubecielo,nubex,nubec,null);
        canvas.drawBitmap(nubecielo,nubex,nubec-nubecielo.getHeight(),null);
        canvas.drawBitmap(nubecielo,nubex,nubec-nubecielo.getHeight()*2,null);
        //xizq
        canvas.drawBitmap(nubecielo,nubex-nubecielo.getWidth(),nubec,null);
        canvas.drawBitmap(nubecielo,nubex-nubecielo.getWidth(),nubec-nubecielo.getHeight(),null);
        canvas.drawBitmap(nubecielo,nubex-nubecielo.getWidth(),nubec-nubecielo.getHeight()*2,null);

        //dibuja el piso
        canvas.drawBitmap(piso,pisox,pisoy,null);

        //dibuja las plataformas
        canvas.drawBitmap(walls[0],wallxy[0][0],wallxy[0][1],null);
        canvas.drawBitmap(walls[1],wallxy[1][0],wallxy[1][1],null);
        canvas.drawBitmap(walls[2],wallxy[2][0],wallxy[2][1],null);
        canvas.drawBitmap(walls[3],wallxy[3][0],wallxy[3][1],null);
        canvas.drawBitmap(walls[4],wallxy[4][0],wallxy[4][1],null);

        //limite de redibujado*******************************************************************
        //redibujar nubes
        if(nubec>dHeight){//si es q la primera nube cielo pasa x debajo de la pantalla
            nubec-=nubecielo.getHeight(); // pone como nuevo limite la sigueinte nuve cielo y el de abajo desaparece
        }
        if(nubex-nubecielo.getWidth()>-20){//muy izq
            nubex-=nubecielo.getWidth();
        }else if(nubex+nubecielo.getWidth()<dWith-20){//muy der
            nubex+=nubecielo.getWidth();
        }

        //redibujar plataformas
        if(wallxy[0][1]>dHeight){//sube en 1 la pos de los walls
            walls[0]=walls[1];wallxy[0][0]=wallxy[1][0];wallxy[0][1]=wallxy[1][1];
            walls[1]=walls[2];wallxy[1][0]=wallxy[2][0];wallxy[1][1]=wallxy[2][1];
            walls[2]=walls[3];wallxy[2][0]=wallxy[3][0];wallxy[2][1]=wallxy[3][1];
            walls[3]=walls[4];wallxy[3][0]=wallxy[4][0];wallxy[3][1]=wallxy[4][1];//crea un nuevo wall para el ultimo
            walls[4]= wallrandom();wallxy[4][0]=wallxy[3][0]+randomx();wallxy[4][1]=wallxy[3][1]-walls[4].getHeight()-randomy();
        }


        //displey del gato
        //esto hace display del gato en la posicion q se encuentra en base a catx y caty q varian con velocidad y gravedad
        canvas.drawBitmap(cat[posicion],catx, caty, null);

        handler.postDelayed(runnable,UPDATE_MILLIS);



        if(inicio!=0){
        if(isGameOver) // Situacion gameOver
        {
            //Dibujar mensaje de game over
            Paint textPaint = new Paint();
            textPaint.setColor(Color.BLACK);
            //Fuente esta en values/dimens.xml
            int fontSize = getResources().getDimensionPixelSize(R.dimen.scoreFontSize);
            textPaint.setTextSize(fontSize);
            //texto Posicion
            canvas.drawText("GAME OVER: \n"+"   "+puntuacion, dWith/2-200,dHeight/2-200, textPaint); //posicion del texto
            textPaint.setTextSize(fontSize);
            if(posicion!=5 && posicion!=4) {//sonido solo una vez
                catdeath.start();
                ost.stop();
            }
            if(dirx>0) //posicion de muerte izq o der
                posicion=5;
            else
                posicion=4;



        }
        else { // Situacion Game normal ***********************************************************


            //sonido gato x puntaje
            if (puntuacion > record) {
                catjump.start();
                record += 50;
                if(UPDATE_MILLIS>1)
                    UPDATE_MILLIS -= 1;
            }

            //Mov Y*************************
            if (isClimbing()) {
                velocidadY = 5;
                velocidadX = 0;
                cantsaltos = 0;
                if(dirx==-1) {
                    posicion = 0;
                }else
                    posicion = 1;
            }
            velocidadY += gravedad; //gravedad ahora aumenta hacia arriba
            if (caty < limy && velocidadY < 0) {//situacion de efecto de camara cuando pasa limite Y o cuando la velocidad de bajada es la misma q te da el click salto
                puntuacion += 1;
                //se mueve lo demas:
                //mueve piso
                pisoy -= velocidadY;
                //mueve nubes
                nubec -= velocidadY;
                nubep -= velocidadY;
                //mueve muerte
                if (muerte < dHeight) // pone el limite de muerte cambia entre piso y aire
                    muerte -= velocidadY; // mientras aun se vea el piso, el piso es muerte
                else
                    muerte = dHeight - cat[0].getHeight() / 2;//cuando no se ve el piso, el cielo es muerte
                //mueven los walls
                wallxy[0][1] -= velocidadY;
                wallxy[1][1] -= velocidadY;
                wallxy[2][1] -= velocidadY;
                wallxy[3][1] -= velocidadY;
                wallxy[4][1] -= velocidadY;
            } else {//se mueve el gato
                if (caty < hmax) {//puntuacion aumenta cuando supera su altura max hasta el limite
                    puntuacion += 1;
                    hmax = caty;
                }
                if (caty + cat[0].getHeight() > muerte)//verifica si se cae al piso GAME OVERRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR
                {
                    velocidadY = 0;

                    isGameOver = true;
                } else {
                    //Movimiento en Y del gato
                    caty += velocidadY;
                }
            }


            //Mov X*****************************
            if (dirbloc == 0) {//si esta fuera de las paredes en los huecos, busca valor de pared cuando entre
                dirbloc = getPared();
            } else {//si ya tiene getpared, toncs verifico si entro al nullo
                if (getPared() == 0)
                    dirbloc = 0;
            }
            if (!isClimbing()) {//verifica q no este en una pared
                if (catx < limxizq && dirx == -1) {//si pasa limite izq y la dir de mov es izq
                    //se mueve lo demas
                    pisox += velocidadX;
                    wallxy[0][0] += velocidadX;
                    wallxy[1][0] += velocidadX;
                    wallxy[2][0] += velocidadX;
                    wallxy[3][0] += velocidadX;
                    wallxy[4][0] += velocidadX;
                    nubex += velocidadX;
                } else if (catx + cat[0].getWidth() > limxder && dirx == 1) {//si pasa limite der y la dir de mov es der
                    //se mueve lo demas
                    pisox -= velocidadX;
                    wallxy[0][0] -= velocidadX;
                    wallxy[1][0] -= velocidadX;
                    wallxy[2][0] -= velocidadX;
                    wallxy[3][0] -= velocidadX;
                    wallxy[4][0] -= velocidadX;
                    nubex -= velocidadX;
                } else {//se mueve el gato
                    //Movimiento en X en base a la direccion q mira
                    catx = catx + velocidadX * dirx;
                }
            }
        }


        }

        //Dibujar SCORE credits to Julius
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        //Fuente esta en values/dimens.xml
        int fontSize = getResources().getDimensionPixelSize(R.dimen.scoreFontSize);
        textPaint.setTextSize(fontSize);
        //texto Puntuación
        canvas.drawText("Puntuacion: "+puntuacion, 20,50, textPaint);
        textPaint.setTextSize(fontSize);

        /*//texto Posicion x
        canvas.drawText("x: "+catx +" ", 20,110, textPaint);
        textPaint.setTextSize(fontSize);
        //texto Posicion y
        canvas.drawText("y: "+caty +" ", 20,160, textPaint);


        canvas.drawText("dirbloc: "+dirbloc +" ", 20,210, textPaint);
        canvas.drawText("isClimbing: "+isClimbing() +" ", 20,260, textPaint);
        canvas.drawText("cantsaltos: "+cantsaltos +" ", 20,310, textPaint);*/

    }








    public boolean onTouchEvent(MotionEvent event){
        int action = event.getAction();
        if(action == MotionEvent.ACTION_DOWN) {//se detecta el click en la pantalla
            velocidadX=30;//reestablece la velocidad del isClimbing
            if(inicio!=0) {
                if (!isGameOver) {//Aca el gato obtiene un boost de velocidad hacia arriba y cambia la dir x en la que se mueve
                    if (cantsaltos == 2) {
                    } else {
                        velocidadY = -65;
                        dirx = dirx * -1;
                        //esto de abajo cambiarlo porque no es x click es cuando ahce click es 1 y cuando toca piso es 0
                        if (posicion == 3) {//cambio continuo de imagen de izq a der
                            posicion = 2;
                        } else {//como pos inicial es 1, el primer cambio es saltar derecha
                            posicion = 3;
                        }
                        cantsaltos += 1;
                    }


                } else {
                    //credits to Juan Perez
                    try {//Hace q se demore en ir a la pantalla principal
                        Thread.sleep(900);//probar valores?
                    } catch (InterruptedException e) {
                        Toast.makeText(getContext(), e.toString(), Toast.LENGTH_LONG).show();
                    }
                    //vuelve a la pantalla principal
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    getContext().startActivity(intent);
                }
            }else{
                velocidadY=-65;
                inicio=1;
                posicion=2;
            }

        }
        return true;
    }







    public Bitmap wallrandom(){
        int random = (int)(Math.random()*2);
        Bitmap wall=null;
        switch(random){
            case 0:
                wall= wall3;
                break;
            case 1:
                wall= wall2;
                break;
            case 2:
                wall= wall1;
                break;
        }
        return wall;
    }

    public int randomx(){
        int x=0;// random entre -210 a 450
        x=(int)(Math.random()*660)-210;
        return x;
    }

    public int randomy(){
        int y=0;// random entre 400 - 500
        y=(int)(Math.random()*100)+400;
        return y;
    }

    public int getPared(){
        for(int i=0; i<5;i++){
            if(caty>=wallxy[i][1] && wallxy[i][1]+walls[i].getHeight()>=caty){ //esta en el mismo y que esta pared
                if(catx<wallxy[i][0])
                    return -1;
                else if(catx>wallxy[i][0])
                    return +1;
            }
            if(caty+cat[0].getHeight()>=wallxy[i][1] && wallxy[i][1]+walls[i].getHeight()>=caty+cat[0].getHeight()){ //esta en el mismo y que esta pared
                if(catx<wallxy[i][0])
                    return -1;
                else if(catx>wallxy[i][0])
                    return +1;
            }
        }
        return 0;
    }

    public boolean isClimbing(){
        for(int i=0; i<5;i++){
            if(caty>=wallxy[i][1] && wallxy[i][1]+walls[i].getHeight()>=caty){ //esta en el mismo y que esta pared
                switch(dirbloc){
                    case -1:
                        if(catx+cat[0].getWidth()>=wallxy[i][0] && dirx==+1)//mueve pa la derecha y ya paso el bloc
                            return true;
                        break;
                    case +1:
                        if(catx<=wallxy[i][0]+walls[i].getWidth() && dirx==-1)//mueve pa la derecha y ya paso el bloc
                            return true;
                        break;
                    case 0:
                        return false;
                }
            }
            if(caty+cat[0].getHeight()>=wallxy[i][1] && wallxy[i][1]+walls[i].getHeight()>=caty+cat[0].getHeight()){ //esta en el mismo y que esta pared
                switch(dirbloc){
                    case -1:
                        if(catx+cat[0].getWidth()>=wallxy[i][0] && dirx==+1)//mueve pa la derecha y ya paso el bloc
                            return true;
                        break;
                    case +1:
                        if(catx<=wallxy[i][0]+walls[i].getWidth() && dirx==-1)//mueve pa la derecha y ya paso el bloc
                            return true;
                        break;
                    case 0:
                        return false;
                }
            }
        }
        return false;
    }


}
