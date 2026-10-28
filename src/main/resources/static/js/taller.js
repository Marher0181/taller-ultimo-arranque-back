// Comportamiento de la interfaz web: el mismo que tenía el front Angular (diálogos, avisos, menús y validación),
// pero sobre páginas renderizadas por el servidor. Los formularios se envían con POST normal.
(() => {
  'use strict';

  const DURACION_AVISO = { exito: 3800, error: 7000, info: 4500 };
  const SALIDA_AVISO_MS = 400;

  // ---------- Avisos (toasts) ----------
  function cerrarAviso(aviso) {
    aviso.classList.add('saliendo');
    setTimeout(() => aviso.remove(), SALIDA_AVISO_MS);
  }

  function programarAviso(aviso) {
    const tipo = ['exito', 'error', 'info'].find((t) => aviso.classList.contains(t)) ?? 'info';
    setTimeout(() => cerrarAviso(aviso), DURACION_AVISO[tipo]);
  }

  function mostrarAviso(tipo, titulo, detalle) {
    const contenedor = document.querySelector('.avisos');
    if (!contenedor) {
      return;
    }
    const aviso = document.createElement('div');
    aviso.className = 'aviso ' + tipo;
    aviso.setAttribute('role', 'status');
    const cuerpo = document.createElement('div');
    const b = document.createElement('b');
    b.textContent = titulo;
    cuerpo.append(b);
    if (detalle) {
      const span = document.createElement('span');
      span.textContent = detalle;
      cuerpo.append(span);
    }
    const cerrar = document.createElement('button');
    cerrar.type = 'button';
    cerrar.className = 'aviso-cerrar';
    cerrar.setAttribute('aria-label', 'Cerrar aviso');
    cerrar.textContent = '×';
    aviso.append(cuerpo, cerrar);
    contenedor.append(aviso);
    programarAviso(aviso);
  }

  // ---------- Formularios ----------
  function lista(valor) {
    return valor === null || valor === undefined ? [] : [].concat(valor).map(String);
  }

  function limpiarErrores(formulario) {
    formulario.querySelectorAll('.field.con-error').forEach((campo) => campo.classList.remove('con-error'));
    formulario.querySelectorAll('.field > .error').forEach((error) => error.remove());
    document.querySelectorAll(`[data-error-de="${formulario.id}"]`).forEach((destino) => {
      destino.textContent = '';
      destino.hidden = true;
    });
  }

  function mostrarErrorCampo(control, mensaje) {
    const campo = control.closest('.field');
    if (!campo) {
      return false;
    }
    campo.classList.add('con-error');
    let error = campo.querySelector(':scope > .error');
    if (!error) {
      error = document.createElement('span');
      error.className = 'error';
      error.setAttribute('role', 'alert');
      campo.append(error);
    }
    error.textContent = mensaje;
    return true;
  }

  function mostrarErrorGeneral(formulario, mensaje) {
    const destino = document.querySelector(`[data-error-de="${formulario.id}"]`);
    if (destino) {
      destino.textContent = mensaje;
      destino.hidden = false;
    } else {
      mostrarAviso('error', 'No se pudo completar la operación', mensaje);
    }
  }

  function asignarValores(formulario, valores) {
    for (const [nombre, valor] of Object.entries(valores)) {
      const controles = formulario.querySelectorAll(`[name="${CSS.escape(nombre)}"]`);
      const lista_ = lista(valor);
      controles.forEach((control) => {
        if (control.type === 'checkbox' || control.type === 'radio') {
          control.checked = lista_.includes(control.value);
        } else if (control.type !== 'hidden' || nombre.startsWith('_')) {
          control.value = lista_[0] ?? '';
        }
      });
    }
  }

  // Elementos que solo aplican al crear o al editar (data-solo="nuevo|editar") y textos según el modo
  function aplicarModo(contenedor, modo) {
    contenedor.querySelectorAll('[data-solo]').forEach((elemento) => {
      const visible = elemento.dataset.solo === modo;
      elemento.hidden = !visible;
      elemento.querySelectorAll('input, select, textarea').forEach((control) => (control.disabled = !visible));
    });
    contenedor.querySelectorAll('[data-bloquear]').forEach((control) => (control.disabled = control.dataset.bloquear === modo));
    contenedor.querySelectorAll('[data-texto-' + modo + ']').forEach((elemento) => (elemento.textContent = elemento.dataset['texto' + modo.charAt(0).toUpperCase() + modo.slice(1)]));
  }

  // Selector que solo se habilita si otro campo tiene cierto valor (data-habilitar-si="rol=MECANICO")
  function aplicarDependencias(formulario) {
    formulario.querySelectorAll('[data-habilitar-si]').forEach((control) => {
      const [campo, valor] = control.dataset.habilitarSi.split('=');
      const origen = formulario.elements[campo];
      const habilitado = origen && origen.value === valor;
      control.disabled = !habilitado;
      if (!habilitado) {
        control.value = '';
      }
    });
  }

  // Mecánicos que ya tienen usuario: se deshabilitan salvo el del usuario que se edita
  function aplicarOcupados(formulario, idActual) {
    formulario.querySelectorAll('option[data-ocupado-por]').forEach((opcion) => {
      const ocupado = opcion.dataset.ocupadoPor !== String(idActual ?? '');
      opcion.disabled = ocupado;
      opcion.textContent = opcion.dataset.textoBase + (ocupado ? ' (ya tiene usuario)' : '');
    });
  }

  function prepararFormulario(formulario, { valores = {}, accion, modo, titulo } = {}) {
    formulario.reset();
    limpiarErrores(formulario);
    if (accion) {
      formulario.action = accion;
    } else if (formulario.dataset.accionBase) {
      formulario.action = formulario.dataset.accionBase;
    }
    if (formulario.elements._modo) {
      formulario.elements._modo.value = modo ?? 'nuevo';
    }
    if (formulario.elements._titulo && titulo) {
      formulario.elements._titulo.value = titulo;
    }
    asignarValores(formulario, valores);
    aplicarDependencias(formulario);
    aplicarOcupados(formulario, lista(valores.id)[0]);
  }

  // ---------- Diálogos ----------
  function abrirDialogo(dialogo, opciones = {}) {
    const formulario = dialogo.querySelector('form');
    const modo = opciones.modo ?? 'nuevo';
    if (formulario) {
      prepararFormulario(formulario, { ...opciones, modo });
    }
    aplicarModo(dialogo, modo);
    const valores = opciones.valores ?? {};
    dialogo.querySelectorAll('[data-texto]').forEach((elemento) => (elemento.textContent = lista(valores[elemento.dataset.texto])[0] ?? ''));
    const titulo = dialogo.querySelector('.dialogo-cabeza h2');
    if (titulo) {
      titulo.textContent = opciones.titulo ?? titulo.dataset.tituloBase ?? titulo.textContent;
    }
    if (!dialogo.open) {
      dialogo.showModal();
    }
    if (opciones.enfocar === false) {
      // showModal() enfoca el botón cerrar; Angular no reabre el diálogo, así que no hay foco visible
      document.activeElement?.blur();
      return;
    }
    setTimeout(() => dialogo.querySelector('.dialogo-cuerpo input:not([disabled]):not([type=hidden]), .dialogo-cuerpo select:not([disabled]), .dialogo-cuerpo textarea')?.focus());
  }

  // ---------- Validación en el navegador (mismos mensajes que el front Angular) ----------
  function mensajeValidez(control) {
    const v = control.validity;
    if (v.valid) return null;
    if (v.valueMissing) return control.dataset.requerido ?? 'Este campo es obligatorio';
    if (control.dataset.mensaje) return control.dataset.mensaje;
    if (v.typeMismatch && control.type === 'email') return 'El correo no tiene un formato válido';
    if (v.rangeUnderflow) return `El valor mínimo es ${control.min}`;
    if (v.rangeOverflow) return `El valor máximo es ${control.max}`;
    if (v.tooLong) return `Máximo ${control.maxLength} caracteres`;
    if (v.tooShort || v.patternMismatch) return 'Valor inválido';
    if (v.stepMismatch || v.badInput) return 'Valor inválido';
    return null;
  }

  // NIT y DPI guatemaltecos: dígito verificador (igual que modelo/validadores.ts del front)
  function nitValido(nit) {
    if (!/^\d{1,12}[0-9K]$/.test(nit)) return false;
    const cuerpo = nit.slice(0, -1);
    const suma = [...cuerpo].reduce((total, digito, i) => total + Number(digito) * (cuerpo.length + 1 - i), 0);
    const residuo = (11 - (suma % 11)) % 11;
    return nit.at(-1) === (residuo === 10 ? 'K' : String(residuo));
  }

  function dpiValido(cui) {
    if (!/^\d{13}$/.test(cui)) return false;
    const departamento = Number(cui.slice(9, 11));
    const municipio = Number(cui.slice(11, 13));
    if (departamento < 1 || departamento > 22 || municipio < 1) return false;
    const suma = [...cui.slice(0, 8)].reduce((total, digito, i) => total + Number(digito) * (i + 2), 0);
    return suma % 11 === Number(cui[8]);
  }

  function mensajePersonalizado(control) {
    if (control.dataset.validar === 'documento' && control.value) {
      const tipo = control.form.elements[control.dataset.tipoDesde].value;
      const valido = tipo === 'NIT' ? nitValido(control.value) : dpiValido(control.value);
      return valido ? null : `El ${tipo} ${control.value} no es válido; revise el dígito verificador`;
    }
    return null;
  }

  function normalizar(formulario) {
    formulario.querySelectorAll('[data-normalizar]').forEach((control) => {
      let valor = control.value.trim();
      if (control.dataset.normalizar === 'sin-guiones') {
        valor = valor.replace(/[\s-]/g, '');
      }
      control.value = valor.toUpperCase();
    });
  }

  document.addEventListener(
    'submit',
    (evento) => {
      const formulario = evento.target;
      if (!(formulario instanceof HTMLFormElement) || formulario.method.toLowerCase() !== 'post') {
        return;
      }
      normalizar(formulario);
      limpiarErrores(formulario);
      let primero = null; // como markAllAsTouched(): marca los errores sin mover el foco
      for (const control of formulario.elements) {
        if (!control.willValidate || control.disabled) continue;
        const mensaje = mensajeValidez(control) ?? mensajePersonalizado(control);
        if (mensaje) {
          if (!mostrarErrorCampo(control, mensaje)) {
            mostrarErrorGeneral(formulario, mensaje);
          }
          primero ??= control;
        }
      }
      if (primero) {
        evento.preventDefault();
        return;
      }
      // Evita envíos dobles mientras carga la siguiente página
      document.querySelectorAll(`button[type=submit][form="${formulario.id}"]`).forEach((boton) => (boton.disabled = true));
      formulario.querySelectorAll('button[type=submit]').forEach((boton) => (boton.disabled = true));
    },
    true,
  );

  // ---------- Clics ----------
  document.addEventListener('click', (evento) => {
    const objetivo = evento.target;

    const abrir = objetivo.closest('[data-abrir]');
    if (abrir) {
      const dialogo = document.getElementById(abrir.dataset.abrir);
      abrirDialogo(dialogo, {
        valores: abrir.dataset.valores ? JSON.parse(abrir.dataset.valores) : {},
        accion: abrir.dataset.accion,
        modo: abrir.dataset.modo,
        titulo: abrir.dataset.titulo,
      });
      return;
    }

    const cerrar = objetivo.closest('[data-cerrar]');
    if (cerrar) {
      cerrar.closest('dialog')?.close();
      return;
    }

    const cerrarAvisoBoton = objetivo.closest('.aviso-cerrar');
    if (cerrarAvisoBoton) {
      cerrarAviso(cerrarAvisoBoton.closest('.aviso'));
      return;
    }

    const fila = objetivo.closest('tr[data-href]');
    if (fila && !objetivo.closest('a, button')) {
      location.href = fila.dataset.href;
      return;
    }

    const menuToggle = objetivo.closest('.menu-toggle');
    if (menuToggle) {
      const sidebar = document.querySelector('.sidebar');
      const abierta = sidebar.classList.toggle('abierta');
      menuToggle.setAttribute('aria-expanded', String(abierta));
      menuToggle.querySelector('[data-icono-menu]').hidden = abierta;
      menuToggle.querySelector('[data-icono-cerrar]').hidden = !abierta;
      return;
    }

    const cuentaBoton = objetivo.closest('.cuenta-boton');
    const menuCuenta = document.querySelector('.menu-cuenta');
    if (cuentaBoton) {
      const abierto = menuCuenta.classList.toggle('abierto');
      cuentaBoton.setAttribute('aria-expanded', String(abierto));
    } else if (menuCuenta && !objetivo.closest('.cuenta')) {
      menuCuenta.classList.remove('abierto');
    }
  });

  document.addEventListener('keydown', (evento) => {
    if (evento.key === 'Escape') {
      document.querySelector('.menu-cuenta')?.classList.remove('abierto');
      document.querySelector('.sidebar')?.classList.remove('abierta');
    }
  });

  document.addEventListener('change', (evento) => {
    const control = evento.target;
    if (control.form) {
      aplicarDependencias(control.form);
    }
    // Filtros que se aplican al cambiar (como ngModelChange → cargar())
    if (control.matches('[data-auto-enviar]')) {
      control.form.requestSubmit();
    }
  });

  // ---------- Filtro de tablas en el navegador ----------
  function filtrarTabla(entrada) {
    const tabla = document.getElementById(entrada.dataset.filtrar);
    const termino = entrada.value.trim().toLowerCase();
    const terminoCompacto = termino.replace(/[\s-]/g, '');
    let visibles = 0;
    const filas = tabla.querySelectorAll('tbody tr[data-buscar]');
    filas.forEach((fila) => {
      const texto = fila.dataset.buscar;
      const coincide = !termino || texto.includes(termino) || texto.includes(terminoCompacto);
      fila.hidden = !coincide;
      visibles += coincide ? 1 : 0;
    });
    const vacio = tabla.querySelector('tbody tr[data-vacio-filtro]');
    if (vacio) {
      vacio.hidden = visibles > 0 || filas.length === 0;
    }
    const total = document.querySelector(`[data-total-de="${tabla.id}"]`);
    if (total) {
      total.textContent = `${visibles} ${visibles === 1 ? total.dataset.singular : total.dataset.plural}`;
    }
  }

  document.addEventListener('input', (evento) => {
    if (evento.target.matches('[data-filtrar]')) {
      filtrarTabla(evento.target);
    }
  });

  // ---------- Al cargar ----------
  document.querySelectorAll('.dialogo-cabeza h2').forEach((titulo) => (titulo.dataset.tituloBase = titulo.textContent));
  document.querySelectorAll('form[action]').forEach((formulario) => (formulario.dataset.accionBase = formulario.getAttribute('action')));
  document.querySelectorAll('form').forEach(aplicarDependencias);
  document.querySelectorAll('.avisos .aviso').forEach(programarAviso);
  document.querySelectorAll('[data-filtrar]').forEach((entrada) => entrada.value && filtrarTabla(entrada));

  // Error devuelto por el servidor: se reabre el diálogo o formulario con lo que se había escrito
  const reabrir = window.TALLER_REABRIR;
  if (reabrir) {
    const formulario = document.getElementById(reabrir.formulario);
    if (formulario) {
      const valores = reabrir.valores ?? {};
      const modo = lista(valores._modo)[0] ?? 'nuevo';
      const titulo = lista(valores._titulo)[0] || undefined;
      const dialogo = formulario.closest('dialog');
      // Como en Angular, el diálogo sigue abierto con el error y el foco no se mueve
      const opciones = { valores, accion: reabrir.accion, modo, titulo, enfocar: false };
      if (dialogo) {
        abrirDialogo(dialogo, opciones);
      } else {
        prepararFormulario(formulario, opciones);
      }
      let aplicado = false;
      for (const [campo, mensaje] of Object.entries(reabrir.errores ?? {})) {
        const control = formulario.elements[campo];
        if (control && !(control instanceof RadioNodeList)) {
          aplicado = mostrarErrorCampo(control, mensaje) || aplicado;
        }
      }
      if (!aplicado) {
        mostrarErrorGeneral(formulario, reabrir.mensaje);
      }
    } else {
      mostrarAviso('error', reabrir.titulo, reabrir.mensaje);
    }
  }
})();
