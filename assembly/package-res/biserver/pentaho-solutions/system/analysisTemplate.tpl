<?xml version="1.0" encoding="UTF-8"?>
<action-sequence> 
  <name>query1.xaction</name>
  <title>My Simple JPivot</title>
  <version>1</version>
  <logging-level>debug</logging-level>
  <documentation> 
    <author>William Seyler</author>  
    <help>just testing...</help>  
    <result-type>report</result-type>  
    <description>Creates a simple Cross Tab</description>  
    <icon>slicedice.png</icon> 
  </documentation>

  <inputs> 
    <mdx type="string"> 
      <default-value/>  
      <sources> 
        <request>MDX</request> 
      </sources> 
    </mdx>  
    <mode type="string"> 
      <default-value/>  
      <sources> 
        <request>mode</request> 
      </sources> 
    </mode> 
  </inputs>

  <outputs>
    <model type="string"/>  
    <connection type="string"/>  
    <mdx type="string"/>  
    <options type="list"/>  
    <title type="string"/>  
    <url type="string"> 
      <destinations> 
        <response>redirect</response> 
      </destinations> 
    </url>  
    <charttype type="string"/>  
    <chartlocation type="string"/>
    <chartheight type="string"/>
    <chartwidth type="string"/> 
  </outputs>

  <resources/>
  
  <actions> 
  
    <action-definition> 
      <component-name>PivotViewComponent</component-name>
      <action-inputs> 
        <mode type="string"/>  
      </action-inputs>
      <action-outputs> 
        <model type="string"/>  
        <connection type="string"/>  
        <mdx type="string"/>  
        <options type="list"/>  
        <title type="string"/>  
        <url type="string"/>  
        <charttype type="string"/>  
        <chartlocation type="string"/>
        <chartheight type="string"/>
        <chartwidth type="string"/>
      </action-outputs>
      <component-definition> 
        <title>Drill Down to Pivot Table</title>  
        <viewer>Pivot</viewer>  
        <model>samples/analysis/SampleData.mondrian.xml</model>  
        <jndi>SampleData</jndi>  
        <query><![CDATA[default]]></query> 
        <charttype>default</charttype>  
        <chartlocation>default</chartlocation>
        <chartwidth>default</chartwidth>
        <chartheight>default</chartheight>
        <options> 
          <save-as/> 
          <cube-nav/>  
          <mdx-edit/>  
          <sort-conf/>  
          <spacer/>  
          <level-style/>  
          <hide-spans/>  
          <properties/>  
          <non-empty/>  
          <swap-axes/>  
          <spacer/>  
          <drill-member/>  
          <drill-position/>  
          <drill-replace/>  
          <drill-thru/>  
          <spacer/>  
          <chart/>  
          <chart-conf/>  
          <spacer/>  
          <print-conf/>  
          <print-pdf/>  
          <spacer/>  
          <excel/> 
        </options>  
      </component-definition>  
      <action-name>Pivot View</action-name>  
      <logging-level>DEBUG</logging-level> 
    </action-definition>
 
  </actions> 
</action-sequence>