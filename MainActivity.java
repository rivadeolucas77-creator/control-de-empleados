package com.controlempleados.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private LinearLayout root, content, nav;
    private SharedPreferences prefs;
    private String page = "Inicio";
    private int selectedEmployee = 0;
    private final String[] defaults = {"Empleado 1", "Empleado 2", "Empleado 3", "Empleado 4", "Empleado 5", "Empleado 6"};
    private final String[] taskDefaults = {"Limpieza", "Reposición de mercadería", "Reposición de fiambres", "Quesos", "Refrigerados", "Congelados", "Temperatura de cámaras de frío", "Reposición cámara de congelados", "Exhibición refrigerados", "Exhibición quesos", "Exhibición fiambres", "Exhibición batea", "Exhibición congelados", "Sectores de salón"};

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("control_empleados_local", MODE_PRIVATE);
        buildShell();
        showPage("Inicio");
    }

    private void buildShell() {
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(0xFFF4F6F8);
        TextView header = new TextView(this); header.setText("CONTROL DE EMPLEADOS"); header.setTextSize(20); header.setTypeface(null, Typeface.BOLD); header.setTextColor(0xFFFFFFFF); header.setBackgroundColor(0xFF123B5D); header.setPadding(18, 22, 18, 22); root.addView(header);
        ScrollView scroll = new ScrollView(this); content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(16, 14, 16, 14); scroll.addView(content); root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        nav = new LinearLayout(this); nav.setOrientation(LinearLayout.HORIZONTAL); nav.setBackgroundColor(0xFFFFFFFF); root.addView(nav);
        String[] pages = {"Inicio", "Empleados", "Tareas", "Horarios", "Informes"};
        for (String p : pages) { Button b = new Button(this); b.setText(p); b.setTextSize(11); b.setPadding(2, 8, 2, 8); b.setOnClickListener(v -> showPage(p)); nav.addView(b, new LinearLayout.LayoutParams(0, -2, 1)); }
        setContentView(root);
    }

    private void showPage(String p) {
        page = p; content.removeAllViews();
        if (p.equals("Inicio")) {
            title("Panel principal"); text("Tus registros se guardan en este celular. No necesitás Internet para usarlos.");
            cardButton("Empleados", "Ver los 6 empleados y sus tareas", () -> showPage("Empleados"));
            cardButton("Tareas de hoy", "Consultar pendientes y realizadas", () -> showPage("Tareas"));
            cardButton("Horarios", "Registrar entrada y salida", () -> showPage("Horarios"));
            cardButton("Informes e historial", "Consultar el avance registrado", () -> showPage("Informes"));
        } else if (p.equals("Empleados")) {
            title("Empleados"); text("Tocá un empleado para ver sus tareas y editar su nombre.");
            for (int i=0;i<6;i++) { final int ix=i; cardButton(employeeName(i), "Abrir ficha del empleado", () -> employeeMenu(ix)); }
        } else if (p.equals("Tareas")) {
            title("Tareas de hoy");
            for (int i=0;i<6;i++) { final int ix=i; cardButton(employeeName(i), completedCount(i)+" de "+taskDefaults.length+" tareas realizadas", () -> { selectedEmployee=ix; showEmployeeTasks(ix); }); }
        } else if (p.equals("Horarios")) {
            title("Entrada y salida"); text("Seleccioná al empleado para registrar el horario actual.");
            for (int i=0;i<6;i++) { final int ix=i; section(employeeName(i)); rowButton("Registrar entrada", () -> saveAttendance(ix,"entrada")); rowButton("Registrar salida", () -> saveAttendance(ix,"salida")); String last = prefs.getString("attendance_"+ix, "Todavía no hay registros."); text(last); }
        } else if (p.equals("Informes")) {
            title("Informes e historial"); text("Resumen guardado en este celular.");
            for (int i=0;i<6;i++) { final int ix=i; section(employeeName(i)); text("Tareas realizadas: "+completedCount(i)+" / "+taskDefaults.length); text("Último registro horario: "+prefs.getString("attendance_"+ix,"Sin registros")); }
            rowButton("Exportar resumen de texto", this::exportSummary);
        }
    }

    private void showEmployeeTasks(int ix) {
        content.removeAllViews(); title(employeeName(ix)+" — tareas");
        rowButton("Editar nombre", () -> editName(ix));
        for (int t=0;t<taskDefaults.length;t++) { final int ti=t; String key=taskKey(ix,t); boolean done=prefs.getBoolean(key,false); section(taskDefaults[t]); text(done?"Estado: REALIZADA":"Estado: PENDIENTE"); rowButton(done?"Marcar pendiente":"Marcar realizada", () -> { prefs.edit().putBoolean(key,!prefs.getBoolean(key,false)).apply(); showEmployeeTasks(ix); }); }
        rowButton("Volver a empleados", () -> showPage("Empleados"));
    }

    private void employeeMenu(int ix) {
        selectedEmployee=ix;
        new AlertDialog.Builder(this).setTitle(employeeName(ix)).setItems(new String[]{"Ver tareas", "Editar nombre"}, (d,which) -> { if(which==0) showEmployeeTasks(ix); else editName(ix); }).setNegativeButton("Cancelar",null).show();
    }
    private void editName(int ix) {
        EditText input = new EditText(this); input.setSingleLine(true); input.setText(employeeName(ix));
        new AlertDialog.Builder(this).setTitle("Nombre del empleado").setView(input).setPositiveButton("Guardar",(d,w)->{ String name=input.getText().toString().trim(); if(!name.isEmpty()) prefs.edit().putString("employee_"+ix,name).apply(); showPage("Empleados"); }).setNegativeButton("Cancelar",null).show();
    }
    private String employeeName(int i) { return prefs.getString("employee_"+i,defaults[i]); }
    private String taskKey(int e,int t) { return "task_"+e+"_"+t; }
    private int completedCount(int e) { int n=0; for(int t=0;t<taskDefaults.length;t++) if(prefs.getBoolean(taskKey(e,t),false)) n++; return n; }
    private void saveAttendance(int ix,String type) { String stamp=new SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.getDefault()).format(new Date()); String old=prefs.getString("attendance_"+ix,""); String line=stamp+" — "+type.toUpperCase(Locale.getDefault()); prefs.edit().putString("attendance_"+ix, old.isEmpty()?line:old+"\n"+line).apply(); Toast.makeText(this,"Horario guardado",Toast.LENGTH_SHORT).show(); showPage("Horarios"); }
    private void exportSummary() { StringBuilder s=new StringBuilder("CONTROL DE EMPLEADOS\n"); for(int i=0;i<6;i++){ s.append("\n").append(employeeName(i)).append("\nTareas realizadas: ").append(completedCount(i)).append(" / ").append(taskDefaults.length).append("\n").append(prefs.getString("attendance_"+i,"Sin registros")).append("\n"); } new AlertDialog.Builder(this).setTitle("Resumen").setMessage(s.toString()).setPositiveButton("Cerrar",null).show(); }
    private void title(String s) { TextView v=new TextView(this); v.setText(s); v.setTextSize(23); v.setTypeface(null,Typeface.BOLD); v.setTextColor(0xFF123B5D); v.setPadding(0,4,0,14); content.addView(v); }
    private void text(String s) { TextView v=new TextView(this); v.setText(s); v.setTextSize(15); v.setTextColor(0xFF394957); v.setPadding(2,5,2,10); content.addView(v); }
    private void section(String s) { TextView v=new TextView(this); v.setText(s); v.setTextSize(16); v.setTypeface(null,Typeface.BOLD); v.setTextColor(0xFF243746); v.setPadding(4,12,4,4); content.addView(v); }
    private void rowButton(String s,Runnable r) { Button b=new Button(this); b.setText(s); b.setAllCaps(false); b.setOnClickListener(v->r.run()); content.addView(b,new LinearLayout.LayoutParams(-1,-2)); }
    private void cardButton(String heading,String detail,Runnable r) { LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(14,10,14,10); box.setBackgroundColor(0xFFFFFFFF); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,0,0,10); box.setLayoutParams(lp); TextView h=new TextView(this); h.setText(heading); h.setTextSize(17); h.setTypeface(null,Typeface.BOLD); h.setTextColor(0xFF123B5D); box.addView(h); TextView d=new TextView(this); d.setText(detail); d.setTextSize(13); d.setTextColor(0xFF52616D); box.addView(d); box.setOnClickListener(v->r.run()); content.addView(box); }
}
