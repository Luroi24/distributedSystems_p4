package proyecto4_2;

import java.util.ArrayList;

class Log{
    public String name;
    public ArrayList<Acceso> accesos;
    public ArrayList<Salida> salidas;
    public ArrayList<Pelicula> peliculas;

    public Log(String name, ArrayList<Acceso> accesos, ArrayList<Salida> salidas, ArrayList<Pelicula> peliculas){
        this.name = name;
        this.accesos = accesos;
        this.salidas = salidas;
        this.peliculas = peliculas;
    }
}

class Acceso{
    public String fecha;
    public String hora;

    public Acceso(String fecha, String hora){
        this.fecha = fecha;
        this.hora = hora;
    }

    @Override
    public String toString(){
        return this.fecha +" "+ this.hora;
    }
}

class Pelicula{
    public String name;
    public String timestamp;

    public Pelicula(String name, String timestamp){
        this.name = name;
        this.timestamp = timestamp;
    }

    @Override
    public String toString(){
        return this.name +" "+ this.timestamp;
    }
}

class Salida{
    public String fecha;
    public String hora;

    public Salida(String fecha, String hora){
        this.fecha = fecha;
        this.hora = hora;
    }

    @Override
    public String toString(){
        return this.fecha +" "+ this.hora;
    }
}

