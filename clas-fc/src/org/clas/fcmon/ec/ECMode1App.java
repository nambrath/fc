package org.clas.fcmon.ec;


import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.clas.fcmon.detector.view.EmbeddedCanvasTabbed;
import org.clas.fcmon.tools.FCApplication;
import org.jlab.detector.base.DetectorCollection;
import org.jlab.detector.base.DetectorDescriptor;
//import org.root.basic.EmbeddedCanvas;
//import org.root.func.F1D;
//import org.root.histogram.H1D;
//groot
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.math.F1D;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;


public class ECMode1App extends FCApplication  {
	
   JPanel              engineView = new JPanel();
   EmbeddedCanvasTabbed    mode1  = new EmbeddedCanvasTabbed("PMT");
   EmbeddedCanvas               c = this.getCanvas(this.getName()); 
   
   int is,la,ic,idet,nstr;
   int ics[][] = new int[3][10];
   String otab[][]={{" U PMT "," V PMT "," W PMT "},
           {" U Inner PMT "," V Inner PMT "," W Inner PMT "},
           {" U Outer PMT "," V Outer PMT "," W Outer PMT "}};
   
   public ECMode1App(String name, ECPixels[] ecPix) {
      super(name,ecPix);	
   }
   
   public JPanel getPanel() {
       engineView.setLayout(new BorderLayout());
       engineView.add(getCanvasPane(),BorderLayout.CENTER);
       return engineView;
   }
   
   public EmbeddedCanvasTabbed getCanvasPane() {
       mode1.addCanvas("UVW");
       mode1.addCanvas("AvsT");
       mode1.addCanvas("SYNC");  
       return mode1;
   }
   
   public void updateCanvas(DetectorDescriptor dd) {
		
      this.is = dd.getSector();
      this.la = dd.getLayer();
      this.ic = dd.getComponent();   
      this.idet = ilmap;	  
      
      if (la>3) return;
      
      this.nstr = ecPix[idet].ec_nstr[la-1];    
      
      switch (mode1.selectedCanvas) {
      case  "PMT": updateEvent(); break;
      case "AvsT": updateAvsT();  break;
      case  "UVW": updateUVW();   break;
      case "SYNC": updateSync();
      }
      
   }
      
   public void updateEvent() {   
   
      int min=0, max=nstr;
      c = mode1.getCanvas("PMT");
      
      switch (idet) {
      case 0: c.divide(4,6); max=24 ; if (ic>23) {min=24; max=48;} if (ic>47) {min=48; max=nstr;} break;
      case 1: c.divide(4,3); max=12 ; if (ic>11) {min=12; max=24;} if (ic>23) {min=24; max=nstr;} break;
      case 2: c.divide(4,3); max=12 ; if (ic>11) {min=12; max=24;} if (ic>23) {min=24; max=nstr;}   
      }   
      
      c.setAxisFontSize(14);
      
//      System.out.println("cr,sl,ch="+app.currentCrate+" "+app.currentSlot+" "+app.currentChan);
//      app.mode7Emulation.configMode7(app.currentCrate, app.currentSlot, app.currentChan);
//      app.mode7Emulation.updateGUI();
      
      int tet = app.mode7Emulation.tet;
      
      if (app.mode7Emulation.User_tet>0)  tet=app.mode7Emulation.User_tet;
      if (app.mode7Emulation.User_tet==0) tet=app.mode7Emulation.CCDB_tet;
      
      F1D f1 = new F1D("p0","[a]",0.,100.); f1.setParameter(0,tet);
      f1.setLineColor(2);
      F1D f2 = new F1D("p0","[a]",0.,100.); f2.setParameter(0,app.mode7Emulation.CCDB_tet);
      f2.setLineColor(4); f2.setLineStyle(2);	
		
      H1F h ;
      
      c.clear();
      
      for(int ip=min;ip<max;ip++){
          c.cd(ip-min); 
          c.getPad(ip-min).setOptStat(Integer.parseInt("0"));
          c.getPad(ip-min).getAxisX().setRange(0.,100.);
          c.getPad(ip-min).getAxisY().setRange(-100.,4000*app.displayControl.pixMax);
          h = ecPix[idet].strips.hmap2.get("H2_Mode1_Sevd").get(is,la,0).sliceY(ip); 
          h.setTitleX("Sector "+is+otab[idet][la-1]+(ip+1)+"  (4 ns/ch)"); h.setTitleY("Counts");
          h.setFillColor(4); c.draw(h);
          h = ecPix[idet].strips.hmap2.get("H2_Mode1_Sevd").get(is,la,1).sliceY(ip); 
          h.setFillColor(2); c.draw(h,"same");
          c.draw(f1,"same"); c.draw(f2,"same");
          }  

      c.repaint();
   }
   
   public void updateUVW() {
       
      DetectorCollection<H2F> dc2a = ecPix[idet].strips.hmap2.get("H2_Mode1_Hist");        
      DetectorCollection<H2F> dc2t = ecPix[idet].strips.hmap2.get("H2_Mode1_Sevd");        
      H1F h1; H2F h2;
     
      F1D f1 = new F1D("p0","[a]",0.,100.); 
      F1D f2 = new F1D("p0","[a]",0.,100.); 
      f1.setParameter(0,ic+1); f1.setLineWidth(1); f1.setLineColor(0);
      f2.setParameter(0,ic+2); f2.setLineWidth(1); f2.setLineColor(0);
             
      c = mode1.getCanvas("UVW");  c.clear(); c.divide(3,2);       
          
      for (int il=1; il<4 ; il++) {
//          h2 = H2F.divide(dc2a.get(is,il,1),dc2a.get(is,il,0)); 
          h2 = dc2a.get(is,il,0);
          h2.setTitleY("Sector "+is+otab[idet][il-1]) ; h2.setTitleX("SAMPLES (4 ns/ch)");         
          canvasConfig(c,il-1,0.,100.,1.,nstr+1.,true).draw(h2);          
          if (la==il) {c.draw(f1,"same"); c.draw(f2,"same");}          
          h1 = dc2a.get(is,il,0).sliceY(ic); h1.setOptStat(Integer.parseInt("1000100"));
          h1.setTitleX("Sector "+is+otab[idet][il-1]+(ic+1)+" (4 ns/ch)"); h1.setFillColor(0);
          c.cd(il+2); h1.setTitle(" "); c.draw(h1);
          if (la==il) {h1=dc2a.get(is,il,0).sliceY(ic) ; h1.setFillColor(2); h1.setOptStat(Integer.parseInt("1000100"));
          h1.setTitleX("Sector "+is+otab[idet][il-1]+(ic+1)+" (4 ns/ch)"); c.draw(h1);}
          if (app.isSingleEvent()) {h1=dc2t.get(is,il,2).sliceY(ic) ; h1.setFillColor(4); c.draw(h1,"same");}
         
      }
      
      c.repaint();
//      ics[idet][la-1]=ic;
      
   }
   
   public void updateAvsT() {
       
       DetectorCollection<H2F> dc2a = ecPix[idet].strips.hmap2.get("H2_a_Hist");       
       
       H2F h2;
       
       c = mode1.getCanvas("AvsT");  c.clear(); c.divide(3,2);       
      
       for (int il=1; il<4; il++) {
           h2=dc2a.get(is,il,4); h2.setTitleY("Sector "+is+otab[idet][il-1]+" TDC") ; h2.setTitleX("Sector "+is+otab[idet][il-1]+" FADC");
           canvasConfig(c,il-1,0.,200.,300.,1200.,true).draw(h2);            
       }
       
       c.repaint();
       
   }
   
   public void updateSync() {
       
       DetectorCollection<H2F> dc2a = ecPix[1].strips.hmap2.get("H2_t_Hist"); 
       H2F h2;
       
       c = mode1.getCanvas("SYNC");  c.clear(); c.divide(3,4); 
       
       for (int is=1; is<7; is++) {
           h2 = dc2a.get(is,3,3) ;  h2.setTitleY("PHASE") ; h2.setTitleX("Sector "+is+" W Inner Raw TDC (ns)");   
           canvasConfig(c,is-1,500.,700.,0.,6.,true).draw(h2);
       }
       for (int is=1; is<7; is++) {
           h2 = dc2a.get(is,3,4) ;  h2.setTitleY("PHASE") ; h2.setTitleX("Sector "+is+" W Inner Corrected TDC (ns)");   
           canvasConfig(c,is-1+6,500.,700.,0.,6.,true).draw(h2);
       }
       
       c.repaint();   
   }
   
}
