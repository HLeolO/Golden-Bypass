# Golden Bypass - Relatorio tecnico

## CVE explorada
CVE-2026-0089 - Missing permission check em PackageInstallerService.createSessionInternal.
Permite a um app local sem INSTALL_PACKAGES criar sessao de instalacao e falsificar
installerPackageName como com.android.vending.

## Versoes afetadas
- Android 14, 15, 16 (antes do patch de setembro/2026).

## Fluxo
1. Usuario acessa index.html no GitHub Pages.
2. Baixa GoldenBypass-signed.apk.
3. Instala manualmente (1 toque).
4. GoldenBypass extrai payload.apk dos assets.
5. Cria PackageInstaller.Session com installerPackageName=com.android.vending.
6. Escreve payload no stream, commit com USER_ACTION_NOT_REQUIRED.
7. Payload instalado silenciosamente e lancado.

## Limitacoes
- Android 8-13: CVE nao se aplica. Fallback requer permissao manual.
- Android 14+ com patch: exploit falha, fallback acionado.
- Play Protect pode detectar o payload. Ofuscar ou desativar (interacao extra).
- Sem root, sem ADB, sem dia zero: nao ha bypass universal.

## Mitigacao
Aplicar patch de seguranca de setembro/2026. Usar perfil de trabalho gerenciado.
