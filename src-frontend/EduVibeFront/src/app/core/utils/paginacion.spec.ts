import { of } from 'rxjs';
import { signal } from '@angular/core';

import { Pagina } from '../models';
import { paginacionLocal, paginarBloques, todasLasPaginas, totalDePaginas, trozo } from './paginacion';

function numeros(cuantos: number): number[] {
  return Array.from({ length: cuantos }, (_, i) => i + 1);
}

describe('paginacion', () => {

  describe('totalDePaginas', () => {
    it('siempre hay al menos una página, aunque no haya elementos', () => {
      expect(totalDePaginas(0)).toBe(1);
    });

    it('redondea hacia arriba', () => {
      expect(totalDePaginas(10)).toBe(1);
      expect(totalDePaginas(11)).toBe(2);
      expect(totalDePaginas(25)).toBe(3);
    });
  });

  describe('trozo', () => {
    it('devuelve los diez elementos de la página pedida', () => {
      expect(trozo(numeros(25), 1)).toEqual([11, 12, 13, 14, 15, 16, 17, 18, 19, 20]);
    });

    it('la última página trae solo lo que queda', () => {
      expect(trozo(numeros(25), 2)).toEqual([21, 22, 23, 24, 25]);
    });

    it('recorta una página que se pasa de la última', () => {
      expect(trozo(numeros(25), 9)).toEqual([21, 22, 23, 24, 25]);
    });

    it('una página negativa se trata como la primera', () => {
      expect(trozo(numeros(25), -3)).toEqual(numeros(10));
    });
  });

  describe('paginarBloques', () => {
    interface Bloque { tema: string; tareas: number[]; }

    const bloques: Bloque[] = [
      { tema: 'A', tareas: numeros(6) },        // 1..6
      { tema: 'B', tareas: [7, 8, 9, 10, 11, 12, 13, 14] },
      { tema: 'C', tareas: [15] },
    ];
    const paginar = (pagina: number) =>
      paginarBloques(bloques, b => b.tareas, (b, tareas) => ({ ...b, tareas }), pagina);

    it('cuenta tareas y no unidades: diez por página', () => {
      const primera = paginar(0);
      expect(primera.flatMap(b => b.tareas).length).toBe(10);
      expect(primera.map(b => b.tema)).toEqual(['A', 'B']);
    });

    it('un bloque cortado entre dos páginas aparece en las dos, con su parte', () => {
      expect(paginar(0)[1].tareas).toEqual([7, 8, 9, 10]);
      expect(paginar(1)[0].tareas).toEqual([11, 12, 13, 14]);
      expect(paginar(1).map(b => b.tema)).toEqual(['B', 'C']);
    });

    it('no repite ni pierde elementos entre páginas', () => {
      const todas = [...paginar(0), ...paginar(1)].flatMap(b => b.tareas);
      expect(todas).toEqual(numeros(15));
    });

    it('no modifica los bloques originales', () => {
      paginar(0);
      expect(bloques[1].tareas.length).toBe(8);
    });
  });

  describe('paginacionLocal', () => {
    it('muestra la primera página y sabe cuántas hay', () => {
      const origen = signal(numeros(23));
      const p = paginacionLocal(() => origen());

      expect(p.visibles()).toEqual(numeros(10));
      expect(p.totalPaginas()).toBe(3);
      expect(p.total()).toBe(23);
      expect(p.pagina()).toBe(0);
    });

    it('cambia de página', () => {
      const origen = signal(numeros(23));
      const p = paginacionLocal(() => origen());

      p.irA(2);

      expect(p.visibles()).toEqual([21, 22, 23]);
      expect(p.pagina()).toBe(2);
    });

    it('si la lista se acorta, la página se recorta sola sin tener que corregirla', () => {
      const origen = signal(numeros(23));
      const p = paginacionLocal(() => origen());
      p.irA(2);

      origen.set(numeros(12));

      expect(p.pagina()).toBe(1);
      expect(p.visibles()).toEqual([11, 12]);
    });

    it('reiniciar vuelve a la primera página', () => {
      const origen = signal(numeros(23));
      const p = paginacionLocal(() => origen());
      p.irA(2);

      p.reiniciar();

      expect(p.pagina()).toBe(0);
    });

    it('con la lista vacía hay una sola página vacía', () => {
      const p = paginacionLocal(() => [] as number[]);

      expect(p.visibles()).toEqual([]);
      expect(p.totalPaginas()).toBe(1);
      expect(p.pagina()).toBe(0);
    });
  });

  describe('todasLasPaginas', () => {
    function pagina(numero: number, total: number, contenido: string[]): Pagina<string> {
      return {
        contenido, pagina: numero, tamano: 2, totalElementos: total,
        totalPaginas: Math.ceil(total / 2), ultima: numero >= Math.ceil(total / 2) - 1,
      };
    }

    it('recorre todas las páginas y junta el contenido en orden', (hecho) => {
      const paginas = [pagina(0, 5, ['a', 'b']), pagina(1, 5, ['c', 'd']), pagina(2, 5, ['e'])];
      const pedidas: number[] = [];

      todasLasPaginas(n => {
        pedidas.push(n);
        return of(paginas[n]);
      }).subscribe(todo => {
        expect(todo).toEqual(['a', 'b', 'c', 'd', 'e']);
        expect(pedidas).toEqual([0, 1, 2]);
        hecho();
      });
    });

    it('con una sola página no pide más', (hecho) => {
      const pedidas: number[] = [];

      todasLasPaginas(n => {
        pedidas.push(n);
        return of(pagina(0, 1, ['solo']));
      }).subscribe(todo => {
        expect(todo).toEqual(['solo']);
        expect(pedidas).toEqual([0]);
        hecho();
      });
    });
  });
});
