# log4j-json-layout

Simple plugin to use layout with log4j2. Usage:


`
<Socket name="LogStashSocket" host="REPLACE_HOST_NAME" port="4560" protocol="tcp">
     <LogstashLayout appname="myTestApplication" charset="UTF-8" includeMDC="true"/>
</Socket>
`
