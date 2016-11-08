<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
	<xsl:output method="html"></xsl:output>
	<xsl:param name="baseUrl" select="''"/>
	
	<xsl:template match="/">
				<style type="text/css">
					body{
						font: 10pt Verdana,sans-serif;
						color: navy;
					}
					.trigger{
						cursor: pointer;
						cursor: hand;
						display: block;
					}
					.branch{
						display: none;
						margin-left: 16px;
					}
					.branch-text-selected{
						text-decoration: none;
						background-color:#7b8622;
						color:#ffffff;
					}
					.branch-text-selected:link,.branch-text-selected:visited{
						text-decoration: none;
						background-color:#7b8622;
						color:#ffffff;
						padding-left:2px;
						padding-right:2px;
					}
					.branch-text{
						text-decoration: none;
						padding-left:2px;
						padding-right:2px;
					}
					.branch-text:hover{
						text-decoration: underline;
						padding-left:2px;
						padding-right:2px;
					}
				</style>
				<script type="text/javascript">
					var openImg = new Image();
					openImg.src = "/pentaho-style/images/btn_minus.png";
					var closedImg = new Image();
					closedImg.src = "/pentaho-style/images/btn_plus.png";
					function showBranch(branch){
						var objBranch = document.getElementById(branch).style;
						if(objBranch.display=="block")
							objBranch.display="none";
						else
							objBranch.display="block";
						swapFolder('I' + branch);
					}
					
					function swapFolder(img){
						objImg = document.getElementById(img);
						if(objImg.src.indexOf('closed.gif')>-1)
							objImg.src = openImg.src;
						else
							objImg.src = closedImg.src;
					}
					
					function getPropsSource(path, isDir) {
						path = "<xsl:value-of select="$baseUrl"/>PropertiesPanel?path=" + path + "&amp;isDir=" + isDir;
						return path;
					}
					
				</script>
			<xsl:apply-templates/>
			
	</xsl:template>
	
	<xsl:template match="error">
		<b>
			<h2>
				<xsl:value-of select="." />
			</h2>
		</b>
	</xsl:template>
	
	<xsl:template match="tree">
			<xsl:apply-templates/>
	</xsl:template>
		
	<xsl:template match="branch">
		<span class="trigger">
			<table>
				<tr>
					<td>
						<img>
							<xsl:attribute name="onClick">showBranch("<xsl:value-of select="@id"/>"); return false;</xsl:attribute>
							<xsl:attribute name="src">/pentaho-style/images/btn_plus.png</xsl:attribute>
							<xsl:attribute name="id">I<xsl:value-of select="@id"/></xsl:attribute>
						</img>
					</td>
					<td>
						<a href="#" id="tree_item" class="branch-text">
							<xsl:attribute name="onClick">window.frames['dataframe'].window.location.replace(  getPropsSource("<xsl:value-of select="@id"/>", '<xsl:value-of select="@isDir"/>') ); var lst=document.getElementsByTagName('a'); for(i=0;i!=lst.length;i++) { if(lst[i].className=='branch-text-selected') lst[i].className='branch-text' } this.className="branch-text-selected"; return false;</xsl:attribute>
							<xsl:value-of select="branchText"/>
						</a>
					</td>
				</tr>
			</table>
		</span>
		<span class="branch">
			<xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
			<xsl:apply-templates/>
		</span>
	</xsl:template>
		
	<xsl:template match="leaf">
		<table>
			<tr>
				<td>
					<img>
						<xsl:attribute name="src">/pentaho-style/images/solution_file.png</xsl:attribute>
					</img>
				</td>
				<td>
					<a ref="#" idXX="tree_item">
						<xsl:attribute name="href" ><xsl:value-of select="link"/></xsl:attribute>
						
						<!-- xsl:attribute name="onClick">window.frames['dataframe'].window.location.replace( getPropsSource("<xsl:value-of select="path"/>", '<xsl:value-of select="@isDir"/>') ); return false; this.class</xsl:attribute -->
						<xsl:attribute name="onClick">window.frames['dataframe'].window.location.replace(  getPropsSource("<xsl:value-of select="path"/>", '<xsl:value-of select="@isDir"/>') ); var lst=document.getElementsByTagName('a'); for(i=0;i!=lst.length;i++) { if(lst[i].className=='branch-text-selected') lst[i].className='branch-text' } this.className="branch-text-selected"; return false;</xsl:attribute>
						
						<!-- xsl:attribute name="onClick">parent.frames['properties'].location.href = getPropsSource("<xsl:value-of select="path"/>", '<xsl:value-of select="@isDir"/>'); return false;</xsl:attribute -->
						<xsl:value-of select="leafText"/>
					</a>
				</td>
			</tr>
		</table>
	</xsl:template>
	

	<!-- avoid output of text node with default template -->
	<xsl:template match="branchText"/>

</xsl:stylesheet>
