---------------------------
## Inicializacao ##
---------------------------

1) Desligar led UV
2) Mover para o endstop

### Funcao Mover para endstop
1) Subir para endstop, em velocidade maxima
2) Parar no endstop
3) Descer 20 passos
4) Subir devagar
5) Parar no endstop
6) Descer para posicao de parking (endstop - 2 mm)


---------------------------
## Configuracao ##
---------------------------

### Se NAO configurada:
- configurar zero do Z 

### ACOES
- Incremento e decremento de 0,1mm
- Incremento e decremento de 1mm
- Incremento e decremento de 10mm
- Guardar Z zero
- Guardar tempo de exposicao por camada
- Guardar tempo de exposicao de primeiras camadas
- Guardar velocidade de subida do peeling
- Guardar numero de camadas de subida do peeling


---------------------------
## Impressao ##
---------------------------

### Mover para Z zero

### Funcao Peeling (0,05mm)
1) Subir quantidade de “camadas de subida do peeling”, na velocidade “velocidade de subida do peeling”
2) Descer quantidade de “camadas de subida do peeling” -1

### Primeiras 8 camadas
1) Mostrar imagem
2) Ligar Led
3) Timer tempo de exposicao de primeiras camadas
4) Desligar Led
5) Fazer Peeling

### Proximas camadas
1) Mostrar imagem
2) Ligar Led
3) Timer tempo de exposicao de primeiras camadas
4) Desligar Led
5) Fazer Peeling

### Finalizacao
1) Mover para endstop


